package com.degrendel.outrogue.agent.goals

import com.degrendel.outrogue.agent.inputs.AutoClean

data class DecideAction(override var accomplished: Boolean = false) : Decision, AutoClean
