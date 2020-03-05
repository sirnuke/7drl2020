package com.degrendel.outrogue.common.world.items

interface Potion: Item
{
  val potionType: PotionType
}

enum class PotionType(val humanName: String)
{
  HEALING("Healing"),
  ;
}