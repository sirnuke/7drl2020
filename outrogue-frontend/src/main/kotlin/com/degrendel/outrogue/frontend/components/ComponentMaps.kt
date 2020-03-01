package com.degrendel.outrogue.frontend.components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

object ComponentMaps
{
  val drawnAt: ComponentMapper<DrawnAtComponent> = ComponentMapper.getFor(DrawnAtComponent::class.java)
}

fun Entity.getDrawnAt() = ComponentMaps.drawnAt.get(this).position