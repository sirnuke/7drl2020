package com.degrendel.outrogue.common.world.items

interface MeleeWeapon : Item
{
  val meleeType: MeleeType
  val damage: Int
  val toHit: Int
}

enum class MeleeType(val humanName: String)
{
  LONG_SWORD("Long Sword"),
  ;
}
