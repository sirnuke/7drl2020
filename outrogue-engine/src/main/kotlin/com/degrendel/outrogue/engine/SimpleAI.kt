package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.agent.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.engine.world.MinionState

object SimpleAI
{
  private val L by logger()

  fun executeSimpleAI(engine: EngineState, minion: MinionState, ai: SimpleController): Action
  {
    L.debug("Executing simple AI {} on minion {}", ai, minion)
    val sources = mutableMapOf<Coordinate, Int>()
    ai.behaviors.forEach { (behavior, weight) ->
      when (behavior)
      {
        Behavior.MOVE_TO_CONJURER -> sources[engine.world.conjurer.coordinate] = weight
        Behavior.MOVE_TO_ROGUE -> sources[engine.world.rogue.coordinate] = weight
      }
    }
    L.debug("Minion {} has sources {}", minion, sources)
    // TODO: Going to need some sort of helper to compute what enemies are available to range attack

    val directions = ai.navigationMap.compute(sources)
        .getBestMoves(minion.coordinate) { _, _, _ -> true }
        .map { (eightWay, coordinate) -> Pair(eightWay, engine.world.getSquare(coordinate)) }

    // Case #1: enemies near by, attack!
    directions.firstOrNull { (_, square) ->
      square.creature?.allegiance?.isHostileTo(minion.allegiance) ?: false
    }?.let {
      return MeleeAttack(minion, it.second.creature!!)
    }

    // Case #2: empty tiles, pick one at random
    directions.firstOrNull { (_, square) ->
      square.creature == null
    }?.let {
      return Move(minion, it.first)
    }

    // Case #3: do nothing, hope the traffic jam clears up
    return Sleep(minion)
  }
}


