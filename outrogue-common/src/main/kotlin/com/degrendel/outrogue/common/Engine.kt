package com.degrendel.outrogue.common

import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Agent
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.World
import com.degrendel.outrogue.common.world.creatures.Creature
import kotlinx.coroutines.Job
import kotlin.random.Random

typealias ECS = com.badlogic.ashley.core.Engine

interface Engine
{
  val world: World
  val ecs: ECS
  val random: Random
  val seed: Long
  val clock: Long

  val rogueAgent: Agent

  val rogueTeam: Family
  val creaturesVisibleToRogue: Family
  val creaturesKnownToRogue: Family
  val squaresVisibleToRogue: Family

  /**
   * Finalizes the initial state of the ECS.
   *
   * Not done in the constructor, since adding entities before the listeners are ready doesn't cause them to fire.
   */
  fun bootstrapECS()

  fun computeCost(action: Action): Long

  fun runGame(): Job

  fun isValidAction(action: Action): Boolean

  fun contextualAction(creature: Creature, eightWay: EightWay): Action?
}