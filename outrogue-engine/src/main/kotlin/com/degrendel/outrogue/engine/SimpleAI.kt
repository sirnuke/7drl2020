package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.ai.SimpleController
import com.degrendel.outrogue.common.ai.Sleep

fun executeSimpleAI(engine: OutrogueEngine, creature: CreatureState, ai: SimpleController): Action
{
  // TODO: Iterate through behaviors, compute list of targets and avoids, generate navigation map
  return Sleep(creature)
}
