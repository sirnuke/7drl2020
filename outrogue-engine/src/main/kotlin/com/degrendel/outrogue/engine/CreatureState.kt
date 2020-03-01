package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Allegiance
import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.Creature
import com.degrendel.outrogue.common.CreatureType
import com.degrendel.outrogue.common.components.*

sealed class CreatureState(final override val entity: Entity, initial: Coordinate) : Creature
{
  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  init
  {
    entity.add(CreatureComponent(this))
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

class Rogue(entity: Entity, initial: Coordinate) : CreatureState(entity, initial)
{
  override val allegiance = Allegiance.ROGUE
  override val type = CreatureType.ROGUE

  init
  {
    updateComponents()
  }
}

class Conjurer(entity: Entity, initial: Coordinate) : CreatureState(entity, initial)
{
  override val allegiance = Allegiance.CONJURER
  override val type = CreatureType.CONJURER

  init
  {
    updateComponents()
  }
}

class Minion(entity: Entity, initial: Coordinate, private var _allegiance: Allegiance, override val type: CreatureType) : CreatureState(entity, initial)
{
  override val allegiance get() = _allegiance

  init
  {
    updateComponents()
  }
}
