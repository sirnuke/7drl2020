package com.degrendel.outrogue.common.agent

import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.items.Item

sealed class Action
{
  abstract val creature: Creature
}

data class Sleep(override val creature: Creature): Action()
data class Move(override val creature: Creature, val direction: EightWay): Action()
data class GoDownStaircase(override val creature: Creature): Action()
data class GoUpStaircase(override val creature: Creature): Action()
data class MeleeAttack(override val creature: Creature, val target: Creature): Action()
data class SwapWith(override val creature: Creature, val target: Creature): Action()
data class PickupItem(override val creature: Creature, val item: Item, val quantity: Int = 1): Action()
data class DropItem(override val creature: Creature, val item: Item, val quantity: Int = 1): Action()
data class EquipItem(override val creature: Creature, val item: Item): Action()
data class DrinkPotion(override val creature: Creature, val item: Item): Action()
data class ReadScroll(override val creature: Creature, val item: Item): Action()
//data class CastSpell
//data class UseAbility
//data class Taunt
//data class RangedAttack
