package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.agent.SimpleController
import com.degrendel.outrogue.common.world.Square.Companion.each
import com.degrendel.outrogue.common.world.Square.Companion.xRange
import com.degrendel.outrogue.common.world.Square.Companion.yRange
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.*
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator
import kotlin.math.pow

class LevelState(val floor: Int, previous: Level?, engine: Engine) : Level
{
  companion object
  {
    private val L by logger()
  }

  private val random = engine.random

  private val squares: List<List<SquareState>>

  private val rooms = mutableListOf<RoomState>()

  override val isFirst = (floor == 0)
  override val isLast = (floor + 1 == P.map.floors)

  private val _downcases = mutableListOf<SquareState>()
  private val _upcases = mutableListOf<SquareState>()

  override val staircasesDown: List<Square> get() = _downcases
  override val staircasesUp: List<Square> get() = _upcases

  init
  {
    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: AbstractRoomGenerator.Room, grid: Grid, value: Float)
      {
        L.debug("Carving room ({},{})->({}x{})", room.x, room.y, room.width, room.height)
        rooms += RoomState(entity = Entity(), topLeft = Coordinate(room.x, room.y, floor), id = rooms.size,
            width = room.width, height = room.height, random = random)
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
          rooms.firstOrNull { it.isWithin(x, y) }?.id
        else null
        SquareState(Coordinate(x, y, floor), type, roomId)
      }
    }

    // Compute the downcases
    if (!isLast)
    {
      val downChance = engine.random.nextDouble()
      val count = P.map.features.extraStaircases.count { downChance < it } + 1
      create(count, count, { !getSquare(it).type.staircase }) {
        squares[it.x][it.y].let { square ->
          square._type = SquareType.STAIRCASE_DOWN
          square._staircase = _downcases.size
          _downcases += square
        }
      }
    }
    // Compute the upcases
    val count = if (isFirst)
    {
      val upChance = engine.random.nextDouble()
      P.map.features.extraStaircases.count { upChance < it } + 1
    }
    else
      previous!!.staircasesDown.size
    create(count, count, { !getSquare(it).type.staircase }) {
      squares[it.x][it.y].let { square ->
        square._type = SquareType.STAIRCASE_UP
        square._staircase = _upcases.size
        _upcases += square
      }
    }

    val walls = mutableListOf<SquareState>()
    val wallify = { x: Int, y: Int ->
      val square = squares[x][y]
      if (square.type.blocked)
      {
        walls += square
        square._type = SquareType.WALL
      }
      else
      {
        square._visible.addAll(square.coordinate.eightWayNeighbors()
            .map { getSquare(it) }
            .filter { it.room != null }
            .also {
              it.forEach { neighbor -> rooms[neighbor.room!!].addDoor(square.coordinate) }
            }
            .mapNotNull { it.room })
        square._type = SquareType.DOOR
      }
    }

    rooms.forEach { room ->
      // TODO: Gross
      for (x in (room.topLeft.x - 1)..(room.topLeft.x + room.width))
      {
        wallify(x, room.topLeft.y - 1)
        wallify(x, room.topLeft.y + room.height)
      }
      for (y in (room.topLeft.y - 1)..(room.topLeft.y + room.height))
      {
        wallify(room.topLeft.x - 1, y)
        wallify(room.topLeft.x + room.width, y)
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

    var monsterBudget = P.map.features.monsterSpawnStart + P.map.features.monsterSpawnScale.pow(floor)
    L.debug("Floor {} has monster budget {}", floor, monsterBudget)

    val monsters = P.creatures.filterValues { it.earliestLevel <= floor }

    while (monsterBudget > 0)
    {
      val toSpawn = monsters.keys.random(engine.random)
      val definition = monsters.getValue(toSpawn)
      val controller = SimpleController(definition.behaviors, (0..0), NavigationMapImpl(engine.random))
      val spawned = create(1, 0, {
        getSquareState(it).let { option: SquareState -> option.creature == null && !option.type.staircase }
      }) {
        spawn(MinionState(Entity(), it, definition.allegiance, toSpawn, controller, 0L, false))
      }
      if (spawned == 0)
      {
        L.warn("Unable to spawn, terminating spawn loop with {} budget left", monsterBudget)
        break
      }
      else
        monsterBudget -= definition.cost * count
    }

    // TODO: Spawn items
  }

  fun spawn(creature: CreatureState)
  {
    squares[creature.coordinate.x][creature.coordinate.y].let { square ->
      assert(creature.coordinate.floor == floor)
      assert(square.creature == null)
      assert(!square.type.blocked)
      square._creature = creature
    }
  }

  fun despawn(creature: CreatureState)
  {
    squares[creature.coordinate.x][creature.coordinate.y].let { square ->
      assert(creature.coordinate.floor == floor)
      assert(square.creature == creature)
      square._creature = null
    }
  }

  fun move(creature: CreatureState, direction: EightWay) = move(creature, creature.coordinate.move(direction))

  fun move(creature: CreatureState, to: Coordinate)
  {
    assert(to.floor == floor)
    assert(squares[creature.coordinate.x][creature.coordinate.y].creature == creature)
    assert(squares[to.x][to.y].creature == null)
    squares[to.x][to.y]._creature = creature
    squares[creature.coordinate.x][creature.coordinate.y]._creature = null
    creature.move(to)
  }

  override fun canMoveCheckingCreatures(from: Coordinate, direction: EightWay) = canMove(from, direction, true)
  override fun canMoveIgnoringCreatures(from: Coordinate, direction: EightWay) = canMove(from, direction, false)

  private fun canMove(from: Coordinate, direction: EightWay, checkCreatures: Boolean): Boolean
  {
    from.move(direction).let { to ->
      val square = getSquare(to)
      return (to.isValid()
          && (!checkCreatures || square.creature == null)
          && !square.type.blocked
          && direction.diagonalChecks
          .map { getSquare(from.x + it.first, from.y + it.second) }
          .all { !it.type.blocked })
    }
  }

  override fun getSquare(x: Int, y: Int): Square = squares[x][y]
  override fun getSquare(coordinate: Coordinate) = getSquare(coordinate.x, coordinate.y)

  fun getSquareState(x: Int, y: Int) = squares[x][y]
  fun getSquareState(coordinate: Coordinate) = getSquareState(coordinate.x, coordinate.y)

  private fun create(count: Int, required: Int, filter: (Coordinate) -> Boolean, action: (Coordinate) -> Unit): Int
  {
    assert(required <= count)
    // TODO: A touch icky and verbose - should be some way to do this functionally
    var amount = 0
    var attempts = 0
    while (amount < count)
    {
      attempts++
      if (attempts > P.map.features.maxPlacementAttempts)
      {
        if (amount < required)
          throw IllegalStateException("Reached max placement attempts - map is probably far too full")
        break
      }
      action(getRandomRooms(1)[0].getRandomSquare(filter) ?: continue)
      amount++
    }
    return amount
  }

  /**
   * Adds the tiles, rooms, and spawned creature entities to the system.
   *
   * This is intended to be done once right after startup, once all families and listeners are wired up and ready to go.
   */
  fun bootstrapECS(ecs: ECS, visibleFloor: Int)
  {
    setOnVisibleLevel(floor == visibleFloor)
    rooms.forEach { ecs.addEntity(it.entity) }
    each { x, y ->
      squares[x][y].let {
        ecs.addEntity(it.entity)
        it.creature?.let { creature -> ecs.addEntity(creature.entity) }
      }
    }
  }

  fun setOnVisibleLevel(visible: Boolean)
  {
    each { x, y ->
      squares[x][y].let {
        it.setOnVisibleLevel(visible)
        it._creature?.setOnVisibleLevel(visible)
      }
    }
  }

  fun getRoom(id: Int) = rooms[id]

  fun getRandomRooms(count: Int) = rooms.shuffled(random).dropLast(rooms.size - count)
}