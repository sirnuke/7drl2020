package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.agent.Action

interface Frontend
{
  suspend fun refreshMap()
  suspend fun getPlayerInput(): Action
}