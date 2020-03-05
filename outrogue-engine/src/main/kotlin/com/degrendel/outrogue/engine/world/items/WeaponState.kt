package com.degrendel.outrogue.engine.world.items

import com.degrendel.outrogue.common.world.Dice
import com.degrendel.outrogue.common.world.items.*

class WeaponState(override val weaponType: WeaponType, private val baseToHit: Int, private val baseDamage: Dice,
                  override var currentlyWhere: ItemLocation, override val weight: Int, initialBonusToHit: Int = 0,
                  initialBonusDamage: Int = 0)
  : Weapon, ItemState
{
  override val type = ItemType.MELEE_WEAPON
  private var _bonusDamage = initialBonusDamage
  override val bonusDamage get() = _bonusDamage
  private var _bonusToHit = initialBonusToHit
  override val bonusToHit get() = _bonusToHit

  override val toHit get() = bonusToHit + baseToHit

  override val stackable get() = false

  override fun canStackWith(other: Item) = false

  override fun getDamage() = baseDamage.roll() + bonusDamage
}