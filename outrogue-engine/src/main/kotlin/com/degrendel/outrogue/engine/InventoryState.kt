package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.world.Inventory
import com.degrendel.outrogue.common.world.InventorySlot
import com.degrendel.outrogue.common.world.items.InInventory
import com.degrendel.outrogue.common.world.items.Item
import com.degrendel.outrogue.engine.world.items.ItemState

class InventoryState : Inventory
{
  private val data = ArrayList<InventorySlotState>()
  private var _weight = 0

  override fun get(slot: Int): InventorySlot?
  {
    return data[slot]
  }

  fun insert(item: ItemState)
  {
    assert(item.where is InInventory)
    _weight += item.weight
    data.firstOrNull { it.canStackWith(item) }?.let {
      (it as InventoryStackState).items.add(item)
      return
    }
    val slot = if (item.stackable)
      InventoryStackState(mutableListOf(item))
    else
      InventorySingleState(item)
    data.add(slot)
  }

  override val slots = data.size
  override val weight get() = _weight
}

sealed class InventorySlotState : InventorySlot
{
  abstract fun canStackWith(other: Item): Boolean
}

data class InventoryStackState(val items: MutableList<ItemState>) : InventorySlotState()
{
  override val item: Item get() = items.first()
  override val count get() = items.size

  override fun canStackWith(other: Item) = item.canStackWith(item)
}

data class InventorySingleState(val itemState: ItemState) : InventorySlotState()
{
  override val count get() = 1
  override val item: Item get() = itemState

  override fun canStackWith(other: Item) = false
}


