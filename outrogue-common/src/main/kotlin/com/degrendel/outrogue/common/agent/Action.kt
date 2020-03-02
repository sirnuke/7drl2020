package com.degrendel.outrogue.common.agent

import com.degrendel.outrogue.common.world.Creature
import com.degrendel.outrogue.common.world.EightWay

sealed class Action
{
  abstract val creature: Creature
}

data class Sleep(override val creature: Creature): Action()
data class Move(override val creature: Creature, val direction: EightWay): Action()
data class GoDownStaircase(override val creature: Creature): Action()
data class GoUpStaircase(override val creature: Creature): Action()
//data class MeleeAttack
//data class RangedAttack
//data class UseItem
//data class ReadScroll
//data class PickupItem
//data class DropItem
//data class EquipItem
