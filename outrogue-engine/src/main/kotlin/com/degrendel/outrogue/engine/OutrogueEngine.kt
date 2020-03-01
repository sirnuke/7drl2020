package com.degrendel.outrogue.engine

import com.degrendel.outrogue.agent.RogueSoarAgent
import com.degrendel.outrogue.common.*

class OutrogueEngine(val frontend: Frontend) : Engine
{
  companion object
  {
    private val L by logger()
  }

  override val ecs = ECS()

  private val soarAgent = RogueSoarAgent()

  private val _world = WorldState(ecs)
  override val world: World get() = _world

  init
  {
    L.info("Creating engine")
  }

  override fun openAgentDebuggers()
  {
    soarAgent.openDebugger()
  }

  override fun bootstrapECS()
  {
    L.info("Bootstrapping ECS")
    _world.bootstrapECS()
    updateECS()
  }

  private fun updateECS()
  {
    L.info("Updating ECS")
    ecs.update(0.0f)
  }
}