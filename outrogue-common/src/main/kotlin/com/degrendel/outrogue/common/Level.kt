package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.properties.Properties.Companion.P

interface Level
{
  fun getSquare(x: Int, y: Int): Square

  fun isNavigable(coordinate: Coordinate): Boolean

  companion object
  {
    fun each(lambda: (x: Int, y: Int) -> Unit) =
      xRange.forEach { x -> yRange.forEach { y -> lambda(x, y) } }

    val xRange = (0 until P.map.width)
    val yRange = (0 until P.map.height)
  }
}