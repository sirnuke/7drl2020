package com.degrendel.outrogue.engine.world.items

import com.degrendel.outrogue.common.world.items.Item
import com.degrendel.outrogue.common.world.items.ItemLocation

interface ItemState: Item
{
  var currentlyWhere: ItemLocation
  override val where get() = currentlyWhere
}