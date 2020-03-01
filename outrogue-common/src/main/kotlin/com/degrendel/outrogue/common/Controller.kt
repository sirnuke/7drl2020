package com.degrendel.outrogue.common

sealed class Controller
object PlayerController: Controller()
object AgentController: Controller()
data class SimpleController(val targetWeight: IntRange): Controller()