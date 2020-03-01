package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.world.World

typealias ECS = com.badlogic.ashley.core.Engine

interface Engine
{
  val world: World
  val ecs: ECS
  fun openAgentDebuggers()

  /**
   * Finalizes the initial state of the ECS.
   *
   * Not done in the constructor, since adding entities before the listeners are ready doesn't cause them to fire.
   */
  fun bootstrapECS()

  fun computeCost(action: Action): Long
}