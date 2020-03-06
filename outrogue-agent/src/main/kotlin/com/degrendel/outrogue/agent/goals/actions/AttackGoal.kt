package com.degrendel.outrogue.agent.goals.actions

import com.degrendel.outrogue.agent.goals.ActionGoal
import com.degrendel.outrogue.common.agent.Action

class AttackGoal : ActionGoal
{
  override var action: Action? = null
  override var evaluated: Boolean = false
  override var applicable: Boolean = false
  override var selected: Boolean = false
  override var accomplished: Boolean = false

  override fun toString(): String
  {
    return "AttackGoal: evaluated? $evaluated accomplished $accomplished applicable $applicable selected? $selected action? $action"
  }
}