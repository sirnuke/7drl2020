package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.world.items.Item

interface Inventory
{
  operator fun get(slot: Int): InventorySlot?
  val slots: Int
  val weight: Int
}

interface InventorySlot
{
  val item: Item
  val count: Int
}