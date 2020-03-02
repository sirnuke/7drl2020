package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.Room
import com.degrendel.outrogue.common.components.CoordinateComponent
import com.degrendel.outrogue.common.components.RoomComponent
import kotlin.random.Random

data class RoomState(override val id: Int, override val entity: Entity, override val topLeft: Coordinate, override val width: Int, override val height: Int, private val random: Random) : Room
{
  private val coordinates = mutableListOf<Coordinate>()

  init
  {
    entity.add(CoordinateComponent(topLeft)).add(RoomComponent(this))
    (topLeft.x until topLeft.x + width).forEach { x ->
      (topLeft.y until topLeft.y + height).forEach { y ->
        coordinates.add(Coordinate(x, y, topLeft.floor))
      }
    }
  }

  fun forEach(lambda: (Coordinate) -> Unit)= coordinates.forEach(lambda)

  override fun isWithin(x: Int, y: Int): Boolean
  {
    return (x >= topLeft.x
        && x < topLeft.x + width
        && y >= topLeft.y
        && y < topLeft.y + height)
  }

  fun getRandomSquare(filter: (coordinate: Coordinate) -> Boolean) =
    coordinates.filter(filter).shuffled(random).firstOrNull()
}