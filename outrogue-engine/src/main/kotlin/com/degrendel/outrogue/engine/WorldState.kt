package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Creature
import com.degrendel.outrogue.common.ECS
import com.degrendel.outrogue.common.Level
import com.degrendel.outrogue.common.Level.Companion.floorRange
import com.degrendel.outrogue.common.World

class WorldState(val ecs: ECS) : World
{
  private val levels = floorRange.map { LevelState(it) }
  private val _conjurer: Conjurer
  private var _rogue: Rogue

  // TODO: Create conjurer and rogue
  init
  {
    levels[0].let { level ->
      level.getRandomRooms(2).let { rooms ->
        assert(rooms.size == 2)
        // TODO: Filter to avoid staircases
        _conjurer = Conjurer(Entity(), rooms[0].getRandomSquare { true }!!)
            .also { level.spawn(it) }
        _rogue = Rogue(Entity(), rooms[1].getRandomSquare { true }!!)
            .also { level.spawn(it) }
      }
    }

    // TODO: Tell levels to spawn initial creatures, items, etc
  }

  override val conjurer: Creature get() = _conjurer
  override val rogue: Creature get() = _rogue

  override fun getLevel(floor: Int): Level = levels[floor]

  fun bootstrapECS()
  {
    levels.forEach { it.bootstrapECS(ecs) }
  }
}