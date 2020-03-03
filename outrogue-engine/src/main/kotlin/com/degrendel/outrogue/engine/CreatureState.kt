package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.agent.AgentController
import com.degrendel.outrogue.common.agent.Controller
import com.degrendel.outrogue.common.agent.PlayerController
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.world.creatures.Allegiance
import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Square
import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.creatures.CreatureType
import com.degrendel.outrogue.common.world.creatures.Rogue
import java.util.concurrent.atomic.AtomicInteger

sealed class CreatureState(final override val entity: Entity, initial: Coordinate, startingClock: Long) : Creature
{
  companion object
  {
    // Probably won't ever be creating creatures in two different threads, but just in case.
    private val nextId = AtomicInteger()
  }

  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  private var _clock = startingClock
  override val clock get() = _clock

  override val id = nextId.getAndIncrement()

  private var _active = false

  override val active get() = _active

  init
  {
    entity.add(CreatureComponent(this))
  }

  fun addCooldown(amount: Long)
  {
    _clock += amount
  }

  // NOTE: Does /not/ remove old allegiance components, that should be handled by the caller
  protected fun updateComponents()
  {
    entity.add(CoordinateComponent(coordinate))
    when (allegiance)
    {
      Allegiance.ROGUE ->
        entity.add(RogueAllegianceComponent).add(VisibleToRogueComponent).add(KnownToRogueComponent)
      Allegiance.CONJURER -> entity.add(ConjurerAllegianceComponent)
      Allegiance.NEUTRAL -> entity.add(NeutralAllegianceComponent)
    }
  }

  fun move(to: Coordinate)
  {
    _coordinate = to
    updateComponents()
  }

  fun setOnVisibleLevel(visible: Boolean)
  {
    if (visible)
      entity.add(OnVisibleLevelComponent)
    else
      entity.remove(OnVisibleLevelComponent::class.java)
  }

  fun setActive(active: Boolean)
  {
    _active = active
    if (active)
      entity.add(ActiveComponent)
    else
      entity.remove(ActiveComponent::class.java)
  }
}

class RogueState(val engine: OutrogueEngine, entity: Entity, initial: Coordinate, clock: Long) : CreatureState(entity, initial, clock), Rogue
{
  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = AgentController

  override val prodded = false

  private val exploreMap = NavigationMapImpl(engine.random) {
    !it.type.blocked && (!it.visibleToRogue || it.creature == null)
  }

  init
  {
    updateComponents()
    setActive(true)
    entity.add(KnownToRogueComponent).add(VisibleToRogueComponent)
  }

  override fun computeExploreDirection(): EightWay?
  {
    // TODO: If other entities get the ability to explore, reuse this map?
    // TODO: TBH, I suspect a simple breadth first search will be sufficient
    val level = engine.world.getLevel(coordinate.floor)
    val sources = mutableMapOf<Coordinate, Int>()
    Square.each { x, y ->
      level.getSquare(x, y).let { if (!it.type.blocked && !it.knownToRogue) sources[it.coordinate] = 0 }
    }
    exploreMap.compute(level, sources, setOf())
    return exploreMap.getBestMove(coordinate)
  }
}

class ConjurerState(entity: Entity, initial: Coordinate, clock: Long) : CreatureState(entity, initial, clock)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController
  override val prodded = false

  init
  {
    updateComponents()
    setActive(true)
  }
}

class MinionState(entity: Entity, initial: Coordinate, private var _allegiance: Allegiance, override val type: CreatureType,
                  initialController: Controller, clock: Long, active: Boolean)
  : CreatureState(entity, initial, clock)
{
  private var _controller = initialController
  override val allegiance get() = _allegiance

  override val controller get() = _controller

  var _prodded = false
  override val prodded get() = _prodded

  init
  {
    updateComponents()
    setActive(active)
  }
}
