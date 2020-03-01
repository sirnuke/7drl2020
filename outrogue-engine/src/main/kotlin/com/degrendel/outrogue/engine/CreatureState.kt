package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.components.*
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

  init
  {
    entity.add(CreatureComponent(this))
  }

  fun addCooldown(amount: Long)
  {
    _cooldown += amount
  }

  protected fun updateComponents()
  {
    entity.add(CoordinateComponent(coordinate))
        .add(AllegianceComponent(allegiance))
  }

  fun move(to: Coordinate)
  {
    _coordinate = to
    updateComponents()
  }

  fun setOnVisibleLevel(visible: Boolean)
  {
    if (visible)
      entity.add(OnVisibleLevel)
    else
      entity.remove(OnVisibleLevel::class.java)
  }
}

class Rogue(entity: Entity, initial: Coordinate, cooldown: Long) : CreatureState(entity, initial, cooldown)
{
  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE
  override val controller = SimpleController((0..0))

  init
  {
    updateComponents()
  }
}

class Conjurer(entity: Entity, initial: Coordinate, cooldown: Long) : CreatureState(entity, initial, cooldown)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER
  override val controller = PlayerController

  init
  {
    updateComponents()
  }
}

class Minion(entity: Entity, initial: Coordinate, private var _allegiance: Allegiance, override val type: CreatureType,
             override val controller: Controller, cooldown: Long)
  : CreatureState(entity, initial, cooldown)
{
  override val allegiance get() = _allegiance

  init
  {
    updateComponents()
  }
}
