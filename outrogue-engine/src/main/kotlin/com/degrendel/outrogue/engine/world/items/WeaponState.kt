package com.degrendel.outrogue.engine.world.items

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.Dice
import com.degrendel.outrogue.common.world.items.*

class WeaponState(entity: Entity, override val weaponType: WeaponType, where: ItemLocation, private val baseToHit: Int, private val baseDamage: Dice,
                  weight: Int, initialBonusToHit: Int = 0, initialBonusDamage: Int = 0)
  : ItemState(entity, ItemType.MELEE_WEAPON, where, weight, stackable = false), Weapon
{
  private var _bonusDamage = initialBonusDamage
  override val bonusDamage get() = _bonusDamage
  private var _bonusToHit = initialBonusToHit
  override val bonusToHit get() = _bonusToHit

  override val toHit get() = bonusToHit + baseToHit

  override fun canStackWith(other: Item) = false

  override fun getDamage() = baseDamage.roll() + bonusDamage
}