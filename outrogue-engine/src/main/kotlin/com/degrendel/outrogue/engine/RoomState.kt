package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.Room
import com.degrendel.outrogue.common.components.CoordinateComponent
import com.degrendel.outrogue.common.components.RoomComponent
import com.degrendel.outrogue.common.logger
import kotlin.random.Random

data class RoomState(override val id: Int, override val entity: Entity, override val topLeft: Coordinate, override val width: Int, override val height: Int, private val random: Random) : Room
{
  companion object
  {
    private val L by logger()
  }

  private val _interior = mutableSetOf<Coordinate>()
  private val _walkable = mutableSetOf<Coordinate>()
  private val _border = mutableSetOf<Coordinate>()
  private val _entire = mutableSetOf<Coordinate>()

  override val interior: Set<Coordinate> get() = _interior
  override val walkable: Set<Coordinate> get() = _walkable
  override val border: Set<Coordinate> get() = _border
  override val entire: Set<Coordinate> get() = _entire

  private val _doors = mutableSetOf<Coordinate>()

  val doors: Set<Coordinate> get() = _doors

  init
  {
    entity.add(CoordinateComponent(topLeft)).add(RoomComponent(this))
    (topLeft.x until topLeft.x + width).forEach { x ->
      (topLeft.y until topLeft.y + height).forEach { y ->
        _interior += Coordinate(x, y, topLeft.floor)
      }
    }
    (topLeft.x - 1 until topLeft.x + width + 1).forEach { x ->
      _border += Coordinate(x, topLeft.y - 1, topLeft.floor)
      _border += Coordinate(x, topLeft.y + height, topLeft.floor)
    }
    (topLeft.y - 1 until topLeft.y + height + 1).forEach { y ->
      _border += Coordinate(topLeft.x - 1, y, topLeft.floor)
      _border += Coordinate(topLeft.x + width, y, topLeft.floor)
    }

    _walkable.addAll(_interior)
    _entire.addAll(_interior)
    _entire.addAll(_border)
  }

  fun addDoor(coordinate: Coordinate)
  {
    _doors += coordinate
    _walkable += coordinate
  }

  override fun isWithin(x: Int, y: Int): Boolean
  {
    return (x >= topLeft.x
        && x < topLeft.x + width
        && y >= topLeft.y
        && y < topLeft.y + height)
  }

  fun getRandomSquare(filter: (coordinate: Coordinate) -> Boolean) =
    interior.filter(filter).shuffled(random).firstOrNull()
}