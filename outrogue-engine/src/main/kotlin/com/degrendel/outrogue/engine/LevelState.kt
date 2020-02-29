package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.Level
import com.degrendel.outrogue.common.Square
import com.degrendel.outrogue.common.properties.Properties.Companion.P

class LevelState(val floor: Int) : Level
{
  private val squares: List<List<SquareState>> = (0 until P.map.width).map { x ->
    (0 until P.map.height).map { y ->
      SquareState(Coordinate(x, y, floor))
    }
  }

  override fun getSquare(x: Int, y: Int): Square = squares[x][y]
}