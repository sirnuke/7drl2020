package com.degrendel.outrogue.engine.world.items

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.items.*

class ArmorState(entity: Entity, override val armorType: ArmorType, val baseAC: Int, where: ItemLocation, weight: Int)
  : ItemState(entity, ItemType.ARMOR, where, weight, stackable = false), Armor
{
  private var currentAC = baseAC
  override val ac get() = currentAC

  override fun canStackWith(other: Item) = false
}
