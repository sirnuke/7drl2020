package com.degrendel.outrogue.common

typealias ECS = com.badlogic.ashley.core.Engine

interface Engine
{
  val world: World
  val ecs: ECS
  fun openAgentDebuggers()
}