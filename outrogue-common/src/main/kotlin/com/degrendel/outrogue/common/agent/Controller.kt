package com.degrendel.outrogue.common.agent

import com.degrendel.outrogue.common.NavigationMap

sealed class Controller
object PlayerController: Controller()
object AgentController: Controller()
data class SimpleController(val behaviors: Map<Behavior, Int>, val targetWeight: IntRange, val navigationMap: NavigationMap): Controller()