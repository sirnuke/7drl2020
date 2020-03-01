package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Allegiance
import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.Creature
import com.degrendel.outrogue.common.components.AllegianceComponent
import com.degrendel.outrogue.common.components.CoordinateComponent

sealed class CreatureState(initial: Coordinate) : Creature
{
  private var _coordinate: Coordinate = initial
  override val coordinate get() = _coordinate

  protected fun updateComponents()
  {
    entity.add(CoordinateComponent(coordinate))
        .add(AllegianceComponent(allegiance))
  }
}

class Rogue(override val entity: Entity, initial: Coordinate) : CreatureState(initial)
{
  override val allegiance = Allegiance.ROGUE
}

class Conjurer(override val entity: Entity, initial: Coordinate) : CreatureState(initial)
{
  override val allegiance = Allegiance.CONJURER
}

class Minion(override val entity: Entity, initial: Coordinate, private var _allegiance: Allegiance) : CreatureState(initial)
{
  override val allegiance get() = _allegiance
}
