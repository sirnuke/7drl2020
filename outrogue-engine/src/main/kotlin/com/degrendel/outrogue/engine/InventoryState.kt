package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.world.Inventory
import com.degrendel.outrogue.common.world.InventorySlot
import com.degrendel.outrogue.common.world.items.Item

class InventoryState : Inventory
{
  private val data = ArrayList<InventorySlot>()
  private var _weight = 0

  override fun get(slot: Int): InventorySlot?
  {
    return data[slot]
  }

  fun insert(item: Item)
  {
    TODO("Stub!")
  }

  override val slots = data.size
  override val weight get() = _weight
}