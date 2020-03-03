package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.properties.Properties.Companion.P

interface Level
{
  val isFirst: Boolean
  val isLast: Boolean

  val staircasesDown: List<Square>
  val staircasesUp: List<Square>

  fun getSquare(x: Int, y: Int): Square
  fun getSquare(coordinate: Coordinate): Square

  companion object
  {
    fun each(lambda: (floor: Int) -> Unit) =
      floorRange.forEach { lambda(it) }

    val floorRange = (0 until P.map.floors)
  }
}