package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.world.creatures.Creature

sealed class LogMessage
data class DescendsStaircaseMessage(val creature: Creature) : LogMessage()
data class AscendStaircaseMessage(val creature: Creature) : LogMessage()
data class MeleeMissMessage(val attacker: Creature, val target: Creature) : LogMessage()
data class MeleeDamageMessage(val attacker: Creature, val target: Creature, val damage: Int) : LogMessage()
data class MeleeDefeatedMessage(val attacker: Creature, val target: Creature): LogMessage()
