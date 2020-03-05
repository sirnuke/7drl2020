package com.degrendel.outrogue.common.world.items

interface Scroll: Item
{
  val scrollType: ScrollType
}

enum class ScrollType(val humanName: String)
{
  ENCHANT_WEAPON("Enchant Weapon"),
  ;
}

