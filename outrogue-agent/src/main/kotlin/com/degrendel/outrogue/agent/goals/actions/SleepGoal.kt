package com.degrendel.outrogue.agent.goals.actions

import com.degrendel.outrogue.agent.goals.ActionGoal
import com.degrendel.outrogue.common.agent.Action

class SleepGoal : ActionGoal
{
  override var accomplished = false
  override var evaluated = true
  override var selected = false
  override var applicable = true
  override var action: Action? = null
}
