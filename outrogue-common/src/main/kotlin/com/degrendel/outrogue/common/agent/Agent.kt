package com.degrendel.outrogue.common.agent

interface Agent
{
  fun enableDebugging()

  suspend fun requestAction(): Action
}