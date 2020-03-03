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

sealed class CreatureState(final override val entity: Entity, initial: Coordinate, initialCooldown: Long) : Creature
{
  companion object
  {
    // Probably won't ever be creating creatures in two different threads, but just in case.
    private val nextId = AtomicInteger()
  }

  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  private var _cooldown = initialCooldown
  override val cooldown get() = _cooldown

  override val id = nextId.getAndIncrement()

  private var _active = false

  override val active get() = _active

  init
  {
    entity.add(CreatureComponent(this))
  }

  fun addCooldown(amount: Long)
  {
    _cooldown += amount
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

class RogueState(val engine: OutrogueEngine, entity: Entity, initial: Coordinate, cooldown: Long) : CreatureState(entity, initial, cooldown), Rogue
{
  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = AgentController

  val exploreMap = NavigationMapImpl(engine.random)

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
    val sources = mutableListOf<Coordinate>()
    Square.each { x, y ->
      level.getSquare(x, y).let { if (!it.type.blocked && !it.knownToRogue) sources += it.coordinate }
    }
    exploreMap.compute(level, sources, setOf())
    return exploreMap.getBestMove(coordinate)
  }
}

class ConjurerState(entity: Entity, initial: Coordinate, cooldown: Long) : CreatureState(entity, initial, cooldown)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController

  init
  {
    updateComponents()
    setActive(true)
  }
}

class MinionState(entity: Entity, initial: Coordinate, private var _allegiance: Allegiance, override val type: CreatureType,
                  initialController: Controller, cooldown: Long, active: Boolean)
  : CreatureState(entity, initial, cooldown)
{
  private var _controller = initialController
  override val allegiance get() = _allegiance

  override val controller get() = _controller

  init
  {
    updateComponents()
    setActive(active)
  }
}
