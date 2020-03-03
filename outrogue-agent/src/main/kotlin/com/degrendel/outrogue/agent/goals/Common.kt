package com.degrendel.outrogue.agent.goals

// NOTE: Goals should be object or class, not data class.  If you modify a member of a data class, the hashcode changes
// which is not what we want.
interface Goal
{
  var accomplished: Boolean
}

interface Decision : Goal

interface ActionGoal : Goal
{
  var evaluated : Boolean
}