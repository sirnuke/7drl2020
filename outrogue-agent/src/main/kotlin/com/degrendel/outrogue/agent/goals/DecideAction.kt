package com.degrendel.outrogue.agent.goals

import com.degrendel.outrogue.agent.inputs.AutoClean

class DecideAction: Decision, AutoClean
{
  override var accomplished: Boolean = false

  override fun toString(): String
  {
    return "DecideActionGoal: accomplished? $accomplished"
  }
}
