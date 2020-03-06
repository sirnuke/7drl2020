package com.degrendel.outrogue.engine.world.items

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.items.*

class PotionState(entity: Entity, override val potionType: PotionType, where: ItemLocation, weight: Int)
  : ItemState(entity, ItemType.POTION, where, weight, stackable = true), Potion
{
  override fun canStackWith(other: Item) = (other is Potion && other.potionType == potionType)
}