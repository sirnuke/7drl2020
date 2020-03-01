package com.degrendel.outrogue.common.ai

sealed class Controller
object PlayerController: Controller()
object AgentController: Controller()
data class SimpleController(val targetWeight: IntRange): Controller()