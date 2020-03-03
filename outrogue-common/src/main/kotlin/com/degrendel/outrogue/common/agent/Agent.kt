package com.degrendel.outrogue.common.agent

interface Agent
{
  fun enableDebugging()

  fun enableLogging()

  suspend fun requestAction(): Action
}