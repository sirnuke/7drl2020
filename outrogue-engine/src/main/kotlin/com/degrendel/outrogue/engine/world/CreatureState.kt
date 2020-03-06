package com.degrendel.outrogue.engine.world

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.ECS
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
  protected abstract var weaponState: WeaponState
  protected abstract var armorState: ArmorState
  final override val weapon: Weapon get() = weaponState
  final override val armor: Armor get() = armorState

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

  fun addToECS(ecs: ECS)
  {
    ecs.addEntity(entity)
    ecs.addEntity(noArmor.entity)
    ecs.addEntity(noWeapon.entity)
    inventoryState.addToECS(ecs)
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

  fun putOnArmor(armor: ArmorState)
  {
    assert(armor.where.let { it is InInventory && it.creature == this })
    armorState = armor
  }

  fun wieldWeapon(weapon: WeaponState)
  {
    assert(weapon.where.let { it is InInventory && it.creature == this })
    weaponState = weapon
  }
}

class RogueState(private val engine: Engine, world: World, entity: Entity, initial: Coordinate, initialClock: Long)
  : CreatureState(entity, initial, maxHp = P.rogue.hp, maxStrength = P.rogue.strength), Rogue
{

  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = AgentController

  override val noArmor = ArmorState(Entity(), ArmorType.NO_ARMOR, P.rogue.ac, InInventory(this), weight = 0)
  override val noWeapon = WeaponState(Entity(), WeaponType.FISTS, InInventory(this), P.rogue.toHit,
      P.rogue.melee.toInstance(engine.random), weight = 0)

  override var armorState: ArmorState = noArmor
  override var weaponState: WeaponState = noWeapon

  // NOTE: Why is world passed in? Because engine.world isn't necessarily initialized, but need engine for other things
  private val exploreMap = NavigationMapImpl(engine.random, world)

  init
  {
    updateComponents()
    setActive(true, initialClock)
    entity.add(KnownToRogueComponent).add(VisibleToRogueComponent)
  }

  override fun computeExploreDirection(): Map<EightWay, Pair<Coordinate, Int>>
  {
    // TODO: If other entities get the ability to explore, reuse this map?
    // TODO: TBH, I suspect a simple breadth first search will be sufficient
    val level = engine.world.getLevel(coordinate.floor)
    val sources = mutableMapOf<Coordinate, Int>()
    Square.each { x, y ->
      level.getSquare(x, y).let { if (!it.type.blocked && !it.knownToRogue) sources[it.coordinate] = 0 }
    }
    if (sources.isEmpty()) return mapOf()
    return exploreMap.compute(sources).getAllMoves(coordinate) { square, _, _ -> !square.type.blocked }
  }
}

class ConjurerState(private val engine: Engine, entity: Entity, initial: Coordinate)
  : CreatureState(entity, initial, maxHp = P.conjurer.hp, maxStrength = P.conjurer.strength), Conjurer
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController

  override val noArmor = ArmorState(Entity(), ArmorType.NO_ARMOR, P.conjurer.ac, InInventory(this), weight = 0)
  override val noWeapon = WeaponState(Entity(), WeaponType.FISTS, InInventory(this), P.rogue.toHit,
      P.conjurer.melee.toInstance(engine.random), weight = 0)

  override var armorState: ArmorState = noArmor
  override var weaponState: WeaponState = noWeapon

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
    // NOTE: Perform navigation without taking into account blocking entities.  When actually selecting the next move,
    // take entities into account (done elsewhere).  This might cause traffic jams and whatnot, but whatever, minions
    // are stupid.  Conjurer (player) and Rogue (Drools agent) should be a bit smarter, which is desirable anyway.
    val navigation = NavigationMapImpl(engine.random, world) { !it.type.blocked }
    _controller = SimpleController(definition.behaviors, (0..0), navigation)

    noArmor = ArmorState(Entity(), ArmorType.NO_ARMOR, definition.ac, InInventory(this), weight = 0)
    noWeapon = WeaponState(Entity(), WeaponType.FISTS, InInventory(this), definition.toHit,
        definition.damage.toInstance(engine.random), weight = 0)
  }

  override var armorState: ArmorState = noArmor
  override var weaponState: WeaponState = noWeapon

  override val controller: Controller get() = _controller

  fun wakeFromContact(clock: Long)
  {
    if (!active)
    {
      L.debug("Set {} active due to contact at {}", this, clock)
      setActive(true, clock)
    }
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
