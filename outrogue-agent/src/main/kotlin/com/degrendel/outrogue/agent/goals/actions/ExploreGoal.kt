package com.degrendel.outrogue.agent.goals.actions

import com.degrendel.outrogue.agent.goals.ActionGoal
import com.degrendel.outrogue.common.agent.Action

class ExploreGoal : ActionGoal
{
  override var evaluated: Boolean = false
  override var accomplished: Boolean = false
  override var applicable: Boolean = false
  override var selected: Boolean = false
  override var action: Action? = null

  override fun toString(): String
  {
    return "ExploreGoal: evaluated? $evaluated accomplished $accomplished applicable $applicable selected? $selected action? $action"
  }
}
