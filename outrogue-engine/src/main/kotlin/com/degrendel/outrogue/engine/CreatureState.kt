package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.agent.AgentController
import com.degrendel.outrogue.common.agent.Controller
import com.degrendel.outrogue.common.agent.PlayerController
import com.degrendel.outrogue.common.agent.SimpleController
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Square
import com.degrendel.outrogue.common.world.World
import com.degrendel.outrogue.common.world.creatures.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

sealed class CreatureState(final override val entity: Entity, initial: Coordinate, override val maxHp: Int) : Creature
{
  companion object
  {
    // Probably won't ever be creating creatures in two different threads, but just in case.
    private val nextId = AtomicInteger()
  }

  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  private var _clock = 0L
  override val clock get() = _clock

  override val id = nextId.getAndIncrement()

  private var _active = false

  override val active get() = _active

  private var _hp = maxHp
  override val hp get() = _hp

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

  protected fun setActive(active: Boolean, clock: Long)
  {
    _active = active
    _clock = clock
    if (active)
      entity.add(ActiveComponent)
    else
      entity.remove(ActiveComponent::class.java)
  }
}

class RogueState(val engine: OutrogueEngine, world: World, entity: Entity, initial: Coordinate, initialClock: Long) : CreatureState(entity, initial, P.rogue.hp), Rogue
{
  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = AgentController

  override val prodded = false

  private val exploreMap = NavigationMapImpl(engine.random, world) {
    !it.type.blocked && (!it.visibleToRogue || it.creature == null)
  }

  init
  {
    updateComponents()
    setActive(true, initialClock)
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
    exploreMap.compute(sources, setOf())
    return exploreMap.getBestMove(coordinate)
  }
}

class ConjurerState(entity: Entity, initial: Coordinate) : CreatureState(entity, initial, P.conjurer.hp)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController
  override val prodded = false

  init
  {
    updateComponents()
    setActive(true, 0L)
  }
}

class MinionState(entity: Entity, initial: Coordinate, private var _allegiance: Allegiance, override val type: CreatureType,
                  initialController: SimpleController, hp: Int)
  : CreatureState(entity, initial, hp)
{
  companion object
  {
    private val L by logger()
  }

  private var _controller = initialController
  override val allegiance get() = _allegiance

  private var _activeStatus = ActiveStatus.ASLEEP
  val activeStatus get() = _activeStatus

  override val controller: Controller get() = _controller

  private var _prodded = false
  override val prodded get() = _prodded

  fun prod(clock: Long)
  {
    L.trace("Prodded {} at {}", this, clock)
    _prodded = true
    if (activeStatus == ActiveStatus.ASLEEP)
    {
      _activeStatus = ActiveStatus.PRODDED
      setActive(true, clock)
    }
  }

  fun wakeFromContact(clock: Long)
  {
    L.trace("Set {} active due to contact at {}", this, clock)
    if (!active)
      setActive(true, clock)
    _activeStatus = ActiveStatus.CONTACT
  }

  init
  {
    updateComponents()
  }
}
