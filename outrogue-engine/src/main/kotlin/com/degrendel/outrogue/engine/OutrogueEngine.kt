package com.degrendel.outrogue.engine

import com.degrendel.outrogue.agent.RogueSoarAgent
import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.PlayerInputProvider
import com.degrendel.outrogue.common.World
import com.degrendel.outrogue.common.logger

class OutrogueEngine(val playerInput: PlayerInputProvider) : Engine
{
  companion object
  {
    private val L by logger()
  }

  private val soarAgent = RogueSoarAgent()

  private val _world = WorldState()
  override val world: World get() = _world

  init
  {
    L.info("Creating engine")
  }

  override fun openAgentDebuggers()
  {
    soarAgent.openDebugger()
  }
}