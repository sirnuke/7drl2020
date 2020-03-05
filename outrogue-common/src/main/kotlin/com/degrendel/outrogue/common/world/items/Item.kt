package com.degrendel.outrogue.common.world.items

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.creatures.Creature

interface Item
{
  val type: ItemType
  val where: ItemLocation
  val stackable: Boolean
  val weight: Int
}

enum class ItemType(val humanName: String)
{
  MELEE_WEAPON("Melee Weapon"),
  ARMOR("Armor"),
  POTION("Potion"),
  SCROLL("Scroll"),
  ;
}

abstract class ItemLocation
data class InInventory(val creature: Creature): ItemLocation()
data class OnGround(val coordinate: Coordinate): ItemLocation()

