package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.agent.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.world.Coordinate
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
    val sources = mutableMapOf<Coordinate, Int>()
    ai.behaviors.forEach { (behavior, weight) ->
      when (behavior)
      {
        Behavior.MOVE_TO_CONJURER -> sources[engine.world.conjurer.coordinate] = weight
        Behavior.MOVE_TO_ROGUE -> sources[engine.world.rogue.coordinate] = weight
      }
    }
    L.debug("Minion {} has sources {}", minion, sources)

    // TODO: A few issues here.  Probably need a navigation map that computes based on ignore creatures, but selects
    //       the best next move taking them into account.
    //       Though if we are right next to a hostile, attack rather than move.
    //       Right now it simply navigates towards the rogue/conjurer, which is okay

    val direction = ai.navigationMap.compute(sources).getBestMove(minion.coordinate).also {
      L.info("computed direction {}", it)
    } ?: return Sleep(minion)
    val candidate = engine.contextualAction(minion, direction)
    return if (candidate == null || candidate is ProdCreature)
      Sleep(minion)
    else
      candidate
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


