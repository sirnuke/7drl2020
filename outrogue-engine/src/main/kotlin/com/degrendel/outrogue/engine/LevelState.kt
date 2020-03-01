package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.Level.Companion.xRange
import com.degrendel.outrogue.common.Level.Companion.yRange
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

class LevelState(val ecs: ECS, val floor: Int) : Level
{
  companion object
  {
    private val L by logger()
  }

  private val squares: List<List<SquareState>>

  private val rooms = mutableListOf<Entity>()

  init
  {

    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: AbstractRoomGenerator.Room, grid: Grid, value: Float)
      {
        L.debug("Carving room ({},{})->({}x{})", room.x, room.y, room.width, room.height)
        rooms += Entity().add(CoordinateComponent(Coordinate(room.x, room.y, floor))).add(RoomComponent(rooms.size, room.width, room.height))
        room.fill(grid, value)
      }

      override fun isValid(room: AbstractRoomGenerator.Room) = true
    })
    dungeonGenerator.minRoomSize = P.map.rooms.minSize
    dungeonGenerator.maxRoomsAmount = P.map.rooms.maxNumber
    val grid = Grid(P.map.width, P.map.height)

    dungeonGenerator.generate(grid)

    squares = xRange.map { x ->
      yRange.map { y ->
        val type = when (grid[x, y])
        {
          1.0f -> SquareType.BLOCKED
          0.5f -> SquareType.FLOOR
          0.0f -> SquareType.CORRIDOR
          else -> throw IllegalStateException("Unexpected value ${grid[x, y]}")
        }
        L.trace("Setting ({},{}) to {}", x, y, type)
        val roomId = if (type == SquareType.FLOOR)
          rooms.firstOrNull { it.isWithinThisRoom(x, y) }?.getRoomData()?.id
        else null
        SquareState(Coordinate(x, y, floor), type, roomId)
      }
    }

    val walls = mutableListOf<SquareState>()
    val wallify = { x: Int, y: Int ->
      val coordinate = Coordinate(x, y, floor)
      if (squares[x][y].type.blocked)
      {
        walls += squares[x][y]
        squares[x][y]._type = SquareType.WALL
      }
      else
      {
        squares[x][y]._visible.addAll(EightWay.values()
            .map { coordinate.move(it) }
            .filter { it.isValid() }
            .mapNotNull { squares[it.x][it.y].room })
        squares[x][y]._type = SquareType.DOOR
      }
    }

    rooms.forEach { room ->
      val topLeft = room.getCoordinate()
      val width: Int
      val height: Int
      room.getRoomData().let { width = it.width; height = it.height }
      // TODO: Gross
      for (x in (topLeft.x - 1)..(topLeft.x + width))
      {
        wallify(x, topLeft.y - 1)
        wallify(x, topLeft.y + height)
      }
      for (y in (topLeft.y - 1)..(topLeft.y + height))
      {
        wallify(topLeft.x - 1, y)
        wallify(topLeft.x + width, y)
      }
    }

    walls.forEach { wall ->
      val neighbors = Cardinal.values()
          .filter {
            val n = wall.coordinate.move(it)
            n.isValid() && squares[n.x][n.y].type.roomBorder
          }.toSet()
      wall._wallOrientation = WallOrientation.lookup.getValue(neighbors)
    }
  }

  override fun isNavigable(coordinate: Coordinate) = squares[coordinate.x][coordinate.y].isNavigable()

  override fun getSquare(x: Int, y: Int): Square = squares[x][y]
}