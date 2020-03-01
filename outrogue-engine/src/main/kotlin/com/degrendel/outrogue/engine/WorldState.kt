package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.ECS
import com.degrendel.outrogue.common.Level
import com.degrendel.outrogue.common.World
import com.degrendel.outrogue.common.properties.Properties.Companion.P

class WorldState(val ecs: ECS) : World
{
  private val levels: List<Level> = (0 until P.map.floors).map { LevelState(ecs, it) }

  override fun getLevel(floor: Int) = levels[floor]
}