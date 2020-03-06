package com.degrendel.outrogue.agent.goals.actions

import com.degrendel.outrogue.agent.goals.ActionGoal
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.creatures.Creature

class AttackGoal : ActionGoal
{
  override var action: Action? = null
  override var evaluated: Boolean = false
  override var applicable: Boolean = false
  override var selected: Boolean = false
  override var accomplished: Boolean = false

  var target: Creature? = null
  var move: EightWay? = null

  override fun toString(): String
  {
    return "AttackGoal: evaluated? $evaluated accomplished $accomplished applicable $applicable selected? $selected action? $action"
  }
}