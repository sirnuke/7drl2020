package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.*
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

  init
  {

    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: AbstractRoomGenerator.Room, grid: Grid, value: Float)
      {
        L.debug("Carving room ({},{})->({}x{})", room.x, room.y, room.width, room.height)
        // TODO: Add room tracking again
        //rooms += Entity().add(PositionComponent(Position(room.x, room.y, floor))).add(RoomComponent(rooms.size, room.width, room.height))
        room.fill(grid, value)
      }

      override fun isValid(room: AbstractRoomGenerator.Room) = true
    })
    dungeonGenerator.minRoomSize = P.map.rooms.minSize
    dungeonGenerator.maxRoomsAmount = P.map.rooms.maxNumber
    val grid = Grid(P.map.width, P.map.height)

    dungeonGenerator.generate(grid)

    squares = (0 until P.map.width).map { x ->
      (0 until P.map.height).map { y ->
        val type = when (grid[x, y])
        {
          1.0f -> SquareType.BLOCKED
          0.5f -> SquareType.FLOOR
          0.0f -> SquareType.CORRIDOR
          else -> throw IllegalStateException("Unexpected value ${grid[x, y]}")
        }
        L.trace("Setting ({},{}) to {}", x, y, type)
        /*
        val roomId = if (type == SquareType.FLOOR)
          rooms.firstOrNull { it.isWithinRoom(position) }?.getRoomData()?.id
        else null
        val visibleRooms = if (roomId == null) setOf() else setOf(roomId)
         */
        SquareState(Coordinate(x, y, floor), type)
      }
    }
  }

  override fun getSquare(x: Int, y: Int): Square = squares[x][y]
}