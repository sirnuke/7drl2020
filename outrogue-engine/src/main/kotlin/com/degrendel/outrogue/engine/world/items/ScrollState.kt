package com.degrendel.outrogue.engine.world.items

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.items.*

class ScrollState(entity: Entity, override val scrollType: ScrollType, where: ItemLocation, weight: Int)
  : ItemState(entity, ItemType.SCROLL, where, weight, stackable = true), Scroll
{
  override fun canStackWith(other: Item) = (other is Scroll && other.scrollType == scrollType)
}