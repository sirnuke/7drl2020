package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.world.creatures.Creature

sealed class AttackResult
{
  abstract val attacker: Creature
  abstract val target: Creature
}

data class MeleeMissedResult(override val attacker: Creature, override val target: Creature) : AttackResult()
data class MeleeLandedResult(override val attacker: Creature, override val target: Creature, val damage: Int) : AttackResult()
data class MeleeDefeatedResult(override val attacker: Creature, override val target: Creature) : AttackResult()
// TODO: Need missile attacks
// TODO: Eventually need dodged (dexterity) versus didn't penetrate (AC) messages
