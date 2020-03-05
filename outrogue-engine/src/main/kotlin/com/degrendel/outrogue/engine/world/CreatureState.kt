package com.degrendel.outrogue.engine.world

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.agent.AgentController
import com.degrendel.outrogue.common.agent.Controller
import com.degrendel.outrogue.common.agent.PlayerController
import com.degrendel.outrogue.common.agent.SimpleController
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.creatures.*
import com.degrendel.outrogue.common.world.items.*
import com.degrendel.outrogue.engine.InventoryState
import com.degrendel.outrogue.engine.NavigationMapImpl
import com.degrendel.outrogue.engine.world.items.ArmorState
import com.degrendel.outrogue.engine.world.items.WeaponState
import java.util.concurrent.atomic.AtomicInteger

sealed class CreatureState(final override val entity: Entity,
                           initial: Coordinate,
                           final override val maxHp: Int,
                           final override val maxStrength: Int)
  : Creature
{
  companion object
  {
    private val L by logger()

    // Probably won't ever be creating creatures in two different threads, but just in case.
    private val nextId = AtomicInteger()
  }

  abstract val noWeapon: WeaponState
  abstract val noArmor: ArmorState
  abstract override val weapon: Weapon
  abstract override val armor: Armor

  // TODO: Implement this based on strength (categories?)
  override val toHit: Int
    get() = 0

  val inventoryState = InventoryState()
  override val inventory: Inventory get() = inventoryState

  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  private var _clock = 0L
  final override val clock get() = _clock

  final override val id = nextId.getAndIncrement()

  private var _active = false

  final override val active get() = _active

  private var _hp = maxHp
  final override val hp get() = _hp

  private var _strength = maxStrength
  final override val strength get() = _strength

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
    L.trace("Creature {} going active? {} clock? {}", this, active, clock)
    _active = active
    _clock = clock
    if (active)
      entity.add(ActiveComponent)
    else
      entity.remove(ActiveComponent::class.java)
  }

  fun applyDamage(damage: Int): DamageResult
  {
    L.trace("Apply damage {} to {}", damage, this)
    _hp -= damage
    return if (_hp <= 0)
      DamageResult.DEFEATED
    else
      DamageResult.SUSTAINED
  }
}

class RogueState(private val engine: Engine, world: World, entity: Entity, initial: Coordinate, initialClock: Long)
  : CreatureState(entity, initial, maxHp = P.rogue.hp, maxStrength = P.rogue.strength), Rogue
{

  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = AgentController

  override val noArmor = ArmorState(ArmorType.NO_ARMOR, P.rogue.ac, InInventory(this), weight = 0)
  override val noWeapon = WeaponState(WeaponType.FISTS, P.rogue.toHit,
      P.rogue.melee.toInstance(engine.random), InInventory(this), weight = 0)

  private var _weapon: Weapon = noWeapon
  override val weapon: Weapon get() = _weapon

  private var _armor: ArmorState = noArmor
  override val armor: Armor get() = _armor

  override val prodded = false

  // NOTE: Why is world passed in? Because engine.world isn't necessarily initialized, but need engine for other things
  private val exploreMap = NavigationMapImpl(engine.random, world)

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
    return exploreMap.compute(sources)
        .getBestMove(coordinate) { square, _, _ -> square.creature == null }?.first
  }
}

class ConjurerState(private val engine: Engine, entity: Entity, initial: Coordinate)
  : CreatureState(entity, initial, maxHp = P.conjurer.hp, maxStrength = P.conjurer.strength)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController
  override val prodded = false

  override val noArmor = ArmorState(ArmorType.NO_ARMOR, P.conjurer.ac, InInventory(this), weight = 0)
  override val noWeapon = WeaponState(WeaponType.FISTS, P.rogue.toHit,
      P.conjurer.melee.toInstance(engine.random), InInventory(this), weight = 0)

  private var _armor = noArmor
  override val armor: Armor get() = _armor

  private var _weapon = noWeapon
  override val weapon: Weapon get() = _weapon

  init
  {
    updateComponents()
    setActive(true, 0L)
  }
}

class MinionState(private val engine: Engine, world: World, entity: Entity, initial: Coordinate,
                  override val type: CreatureType)
  : CreatureState(entity, initial, maxHp = P.creatures.getValue(type).hp,
    maxStrength = P.creatures.getValue(type).strength)
{
  companion object
  {
    private val L by logger()
  }

  private var _controller: SimpleController

  private var _allegiance: Allegiance
  override val allegiance get() = _allegiance
  override val noArmor: ArmorState
  override val noWeapon: WeaponState

  init
  {
    val definition = P.creatures.getValue(type)
    _allegiance = definition.allegiance
    _controller = SimpleController(definition.behaviors,
        definition.targetWeight.toInstance(), NavigationMapImpl(engine.random, world))

    noArmor = ArmorState(ArmorType.NO_ARMOR, definition.ac, InInventory(this), weight = 0)
    noWeapon = WeaponState(WeaponType.FISTS, definition.toHit, definition.damage.toInstance(engine.random),
        InInventory(this), weight = 0)
  }

  // TODO: These assume minions can't equip random things - not (yet) true
  override val weapon: Weapon get() = noWeapon
  override val armor: Armor get() = noArmor

  private var _activeStatus = ActiveStatus.ASLEEP
  val activeStatus get() = _activeStatus

  override val controller: Controller get() = _controller

  private var _prodded = false
  override val prodded get() = _prodded

  fun prod(clock: Long)
  {
    L.debug("Prodded {} at {}", this, clock)
    _prodded = true
    if (activeStatus == ActiveStatus.ASLEEP)
    {
      _activeStatus = ActiveStatus.PRODDED
      setActive(true, clock)
    }
  }

  fun unprod()
  {
    L.debug("Prod on {} complete", this)
    _prodded = false
    if (activeStatus == ActiveStatus.PRODDED)
    {
      _activeStatus == ActiveStatus.ASLEEP
      setActive(false, clock)
    }
  }

  fun wakeFromContact(clock: Long)
  {
    L.debug("Set {} active due to contact at {}", this, clock)
    if (!active)
      setActive(true, clock)
    _activeStatus = ActiveStatus.CONTACT
  }

  init
  {
    updateComponents()
  }
}

enum class DamageResult
{
  SUSTAINED,
  DEFEATED,
  ;
}
