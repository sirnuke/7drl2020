package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.ECS
import com.degrendel.outrogue.common.world.Inventory
import com.degrendel.outrogue.common.world.InventorySlot
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

  fun addToECS(ecs: ECS)
  {
    data.forEach { ecs.addEntity(it.item.entity) }
  }

  fun insert(item: ItemState, quantity: Int = 1)
  {
    _weight += (item.weight * quantity)
    data.firstOrNull { it.canStackWith(item) }?.let {
      (it as InventoryStackState).increase(quantity)
      return
    }
    val slot = if (item.stackable)
      InventoryStackState(item, quantity)
    else
      InventorySingleState(item)
    data.add(slot)
  }

  fun remove(item: ItemState, quantity: Int = 1)
  {
    assert(quantity > 0)
    assert(quantity == 1 || item.stackable)
    _weight -= item.weight * quantity
    assert(_weight >= 0)
    return when (val slot = data.first { it == item || it.canStackWith(item) })
    {
      is InventorySingleState ->
      {
        assert(quantity == 1)
        data.remove(slot)
        Unit
      }
      is InventoryStackState ->
      {
        assert(quantity <= slot.count)
        if (quantity == slot.count)
          data.remove(slot)
        else
          slot.decrease(quantity)
        Unit
      }
    }
  }

  override val slots = data.size
  override val weight get() = _weight
}

sealed class InventorySlotState : InventorySlot
{
  abstract fun canStackWith(other: Item): Boolean
}

data class InventoryStackState(val itemState: ItemState, private var _count: Int) : InventorySlotState()
{
  override val item: Item get() = itemState
  override val count get() = _count

  fun increase(quantity: Int)
  {
    assert(quantity > 0)
    _count += quantity
  }

  fun decrease(quantity: Int)
  {
    assert(quantity < count)
    _count -= quantity
  }

  override fun canStackWith(other: Item) = item.canStackWith(item)
}

data class InventorySingleState(val itemState: ItemState) : InventorySlotState()
{
  override val count get() = 1
  override val item: Item get() = itemState

  override fun canStackWith(other: Item) = false
}


