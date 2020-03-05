package com.degrendel.outrogue.common.world.items

interface Weapon : Item
{
  val weaponType: WeaponType
  val bonusToHit: Int
  val bonusDamage: Int
  val toHit: Int
}

enum class WeaponType(val humanName: String)
{
  FISTS("Unarmed"),
  LONG_SWORD("Long Sword"),
  ;
}
