package com.degrendel.outrogue.common.world.items

interface Armor: Item
{
  val armorType: ArmorType
  val ac: Int
}

enum class ArmorType(val humanName: String)
{
  LEATHER_ARMOR("Leather Armor"),
  ;
}
