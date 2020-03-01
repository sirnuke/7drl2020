package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.ai.Action

interface Frontend
{
  suspend fun refreshMap()
  suspend fun getPlayerInput(): Action
}