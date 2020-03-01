package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.properties.Properties.Companion.P

interface Level
{
  fun getSquare(x: Int, y: Int): Square

  fun isNavigable(coordinate: Coordinate): Boolean

  companion object
  {
    fun each(lambda: (floor: Int) -> Unit) =
      floorRange.forEach { lambda(it) }

    val floorRange = (0 until P.map.floors)
  }
}