package com.degrendel.outrogue.engine.items

import com.degrendel.outrogue.common.world.items.Armor
import com.degrendel.outrogue.common.world.items.ArmorType
import com.degrendel.outrogue.common.world.items.ItemLocation
import com.degrendel.outrogue.common.world.items.ItemType

data class ArmorState(override val armorType: ArmorType, var currentAC: Int, var currentlyWhere: ItemLocation, override val weight: Int) : Armor
{
  override val where get() = currentlyWhere
  override val stackable = false
  override val type = ItemType.ARMOR
  override val ac get() = currentAC
}
