package com.degrendel.outrogue.common.world.items

interface Armor: Item
{
  val armorType: ArmorType
  val ac: Int
}

enum class ArmorType(val humanName: String)
{
  NO_ARMOR("No Armor"),
  LEATHER_ARMOR("Leather Armor"),
  ;
}
