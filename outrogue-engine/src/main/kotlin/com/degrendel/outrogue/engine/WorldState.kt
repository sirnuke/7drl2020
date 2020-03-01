package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.ECS
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.Level.Companion.floorRange

class WorldState(val engine: OutrogueEngine) : World
{
  private val levels: List<LevelState>

  init
  {
    // TODO: Ick
    var previous: LevelState? = null
    levels = floorRange.map { previous = LevelState(it, previous, engine); previous!! }
  }

  private val _conjurer: Conjurer
  private var _rogue: Rogue

  // TODO: Create conjurer and rogue
  init
  {
    levels[0].let { level ->
      level.getRandomRooms(2).let { rooms ->
        assert(rooms.size == 2)
        // TODO: Filter to avoid staircases
        _conjurer = Conjurer(Entity(), rooms[0].getRandomSquare { true }!!, 0L)
            .also { level.spawn(it) }
        _rogue = Rogue(Entity(), rooms[1].getRandomSquare { true }!!, 0L)
            .also { level.spawn(it) }
      }
    }

    // TODO: Tell levels to spawn initial creatures, items, etc
  }

  override val conjurer: Creature get() = _conjurer
  override val rogue: Creature get() = _rogue

  override fun getLevel(floor: Int): Level = levels[floor]

  override fun getSquare(coordinate: Coordinate) = levels[coordinate.floor].getSquare(coordinate)

  fun getLevel(creature: Creature) = levels[creature.coordinate.floor]

  fun bootstrapECS()
  {
    levels.forEach { it.bootstrapECS(engine.ecs, conjurer.coordinate.floor) }
  }
}