package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.world.World
import kotlinx.coroutines.Job
import kotlin.random.Random

typealias ECS = com.badlogic.ashley.core.Engine

interface Engine
{
  val world: World
  val ecs: ECS
  val random: Random
  val seed: Long

  fun openAgentDebuggers()

  /**
   * Finalizes the initial state of the ECS.
   *
   * Not done in the constructor, since adding entities before the listeners are ready doesn't cause them to fire.
   */
  fun bootstrapECS()

  fun computeCost(action: Action): Long

  fun runGame(): Job

  fun isValidAction(action: Action): Boolean
}