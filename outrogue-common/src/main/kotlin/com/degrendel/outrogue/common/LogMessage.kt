package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.world.creatures.Creature

sealed class LogMessage
data class DescendsStaircaseMessage(val creature: Creature): LogMessage()
data class AscendStaircaseMessage(val creature: Creature): LogMessage()
data class MeleeMissMessage(val attacker: Creature, val target: Creature): LogMessage()
