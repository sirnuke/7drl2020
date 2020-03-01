package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.Square
import com.degrendel.outrogue.common.SquareType
import com.degrendel.outrogue.common.WallOrientation
import com.degrendel.outrogue.common.components.CoordinateComponent
import com.degrendel.outrogue.common.components.SquareComponent

class SquareState(override val coordinate: Coordinate, var _type: SquareType, override val room: Int?, var creature: CreatureState? = null) : Square
{
  var _visible = mutableSetOf<Int>().also { if (room != null) it.add(room) }
  override val visible: Set<Int> get() = _visible
  override val entity: Entity = Entity()
      .add(CoordinateComponent(coordinate))
      .add(SquareComponent(this))

  override val type: SquareType get() = _type

  var _wallOrientation = WallOrientation.NONE

  override val wallOrientation get() = _wallOrientation

  override fun isNavigable() = _type.blocked && creature == null
}