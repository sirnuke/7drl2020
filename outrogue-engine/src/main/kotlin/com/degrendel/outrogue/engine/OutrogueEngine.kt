package com.degrendel.outrogue.engine

import com.degrendel.outrogue.agent.RogueSoarAgent
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.ai.Move
import com.degrendel.outrogue.common.ai.Sleep
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.World

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

  private val actionQueue = ActionQueueSystem(this)

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

  override fun computeCost(action: Action): Long
  {
    return when (action)
    {
      is Sleep -> P.costs.sleep
      // TODO: Charge more for diagonal?
      is Move -> P.costs.move
    }
  }
}