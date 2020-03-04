package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.world.creatures.Creature

interface Frontend
{
  suspend fun refreshMap()
  suspend fun getPlayerInput(): Action

  fun drawNavigationMap(map: NavigationMap)
  fun drawDebug(x: Int, y: Int, value: Int)

  fun addLogMessages(messages: List<LogMessage>)
}

sealed class LogMessage
{
  abstract val message: String
}
data class CreatureLogMessage(val sender: Creature, override val message: String): LogMessage()
