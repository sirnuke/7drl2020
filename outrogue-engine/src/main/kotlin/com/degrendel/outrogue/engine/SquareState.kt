package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.Square
import com.degrendel.outrogue.common.SquareType
import com.degrendel.outrogue.common.WallOrientation
import com.degrendel.outrogue.common.components.CoordinateComponent
import com.degrendel.outrogue.common.components.SquareComponent

class SquareState(override val coordinate: Coordinate, override val type: SquareType) : Square
{
  override val entity: Entity = Entity()
      .add(CoordinateComponent(coordinate))
      .add(SquareComponent(this))

  var _wallOrientation = WallOrientation.NONE

  override val wallOrientation get() = _wallOrientation
}