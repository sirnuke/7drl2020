package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.properties.Properties.Companion.P

data class Coordinate(val x: Int, val y: Int, val floor: Int)
{
  fun move(direction: Cardinal) = Coordinate(x + direction.x, y + direction.y, floor)

  fun move(direction: EightWay) = Coordinate(x + direction.x, y + direction.y, floor)

  fun isValid(): Boolean
  {
    return (x >= 0
        && x < P.map.width
        && y >= 0
        && y < P.map.height
        && floor >= 0
        && floor < P.map.floors
        )
  }
}
