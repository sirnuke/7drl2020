package com.degrendel.outrogue.common.world.items

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.creatures.Creature

interface Item
{
  val type: ItemType
  val where: ItemLocation
}

enum class ItemType(val humanName: String)
{
  MELEE_WEAPON("Melee Weapon"),
  ;
}

abstract class ItemLocation
data class InInventory(val creature: Creature): ItemLocation()
data class OnGround(val coordinate: Coordinate): ItemLocation()
data class OnCreature(val creature: Creature): ItemLocation()

