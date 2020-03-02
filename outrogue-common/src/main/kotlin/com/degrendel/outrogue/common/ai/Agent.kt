package com.degrendel.outrogue.common.ai

interface Agent
{
  fun openDebugger()

  suspend fun requestAction(): Action
}