package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.ECS
import com.degrendel.outrogue.common.Level
import com.degrendel.outrogue.common.Level.Companion.floorRange
import com.degrendel.outrogue.common.World

class WorldState(val ecs: ECS) : World
{
  private val levels = floorRange.map { LevelState(ecs, it) }

  override fun getLevel(floor: Int): Level = levels[floor]
}