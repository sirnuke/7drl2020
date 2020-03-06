package com.degrendel.outrogue.agent.goals.actions

import com.degrendel.outrogue.agent.goals.ActionGoal
import com.degrendel.outrogue.common.agent.Action

class SleepGoal : ActionGoal
{
  override var accomplished = false
  override var evaluated = false
  override var selected = false
  override var applicable = false
  override var action: Action? = null
}
