package com.degrendel.outrogue.engine.world.items

import com.degrendel.outrogue.common.world.items.*

data class ArmorState(override val armorType: ArmorType, var currentAC: Int, override var currentlyWhere: ItemLocation, override val weight: Int) : Armor, ItemState
{
  override val stackable = false
  override val type = ItemType.ARMOR
  override val ac get() = currentAC

  override fun canStackWith(other: Item) = false
}
