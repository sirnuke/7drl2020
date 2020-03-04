package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Move
import com.degrendel.outrogue.common.agent.SimpleController
import com.degrendel.outrogue.common.agent.Sleep
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.world.EightWay

object SimpleAI
{
  private val L by logger()

  fun executeSimpleAI(engine: OutrogueEngine, minion: MinionState, ai: SimpleController): Action
  {
    L.debug("Executing simple AI {} on minion {}", ai, minion)
    // Responding to a prod always comes first
    if (minion.prodded)
    {
      minion.unprod()
      return executeProd(engine, minion)
    }
    // TODO: Iterate through behaviors, compute list of targets and avoids, generate navigation map
    return Sleep(minion)
  }

  private fun executeProd(engine: OutrogueEngine, minion: MinionState): Action
  {
    L.info("Minion {} is executing prod", minion)
    val level = engine.world.getLevel(minion.coordinate)
    val options = EightWay.values().filter {
      val dir = minion.coordinate.move(it)
      if (!dir.isValid()) return@filter false
      val square = level.getSquare(dir)
      !square.type.blocked && square.creature == null
    }
    // TODO: There's room for some heuristics here - move away from doors, away from prodder.
    val selected = options.shuffled(engine.random).firstOrNull()
    return if (selected == null)
      Sleep(minion)
    else
      Move(minion, selected)
  }
}


