package com.degrendel.outrogue.common.ai

import com.degrendel.outrogue.common.NavigationMap

sealed class Controller
object PlayerController: Controller()
object AgentController: Controller()
data class SimpleController(val behaviors: List<Behavior>, val targetWeight: IntRange, val navigationMap: NavigationMap): Controller()