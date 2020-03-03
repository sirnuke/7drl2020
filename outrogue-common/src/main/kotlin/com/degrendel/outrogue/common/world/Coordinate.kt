package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.properties.Properties.Companion.P
import kotlin.math.abs

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

  fun canInteract(world: World, other: Coordinate): Boolean
  {
    if (floor != other.floor) return false
    val deltaX = abs(x - other.x)
    val deltaY = abs(y - other.y)
    return if (deltaX > 1 || deltaY > 1)
      false
    else if (deltaX == 0 || deltaY == 0)
      true
    else
      (!world.getSquare(x, other.y, floor).type.blocked
          && !world.getSquare(other.x, y, floor).type.blocked)
  }

  fun eightWayNeighbors(): List<Coordinate> = EightWay.values().map { move(it) }.filter { it.isValid() }
}
