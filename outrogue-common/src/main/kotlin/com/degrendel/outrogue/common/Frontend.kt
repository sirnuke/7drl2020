package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.agent.Action

interface Frontend
{
  suspend fun refreshMap()
  suspend fun getPlayerInput(): Action

  fun drawNavigationMap(map: NavigationMap)
  fun drawDebug(x: Int, y: Int, value: Int)

  fun addLogMessages(messages: List<LogMessage>)
}

