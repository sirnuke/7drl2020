package com.degrendel.outrogue.engine.world.items

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.components.CoordinateComponent
import com.degrendel.outrogue.common.components.ItemComponent
import com.degrendel.outrogue.common.world.items.*

abstract class ItemState(final override val entity: Entity, final override val type: ItemType,
                         private var currentlyWhere: ItemLocation, final override val weight: Int,
                         final override val stackable: Boolean) : Item
{
  final override val where get() = currentlyWhere

  protected fun updateComponents()
  {
    val coordinate = currentlyWhere.let {
      when (it)
      {
        is OnGround -> it.coordinate
        is InInventory -> it.creature.coordinate
      }
    }
    entity.add(ItemComponent(this)).add(CoordinateComponent(coordinate))
  }
}