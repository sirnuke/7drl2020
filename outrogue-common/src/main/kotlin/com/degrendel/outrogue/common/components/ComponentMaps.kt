package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.ComponentMapper

object ComponentMaps
{
  val coordinate: ComponentMapper<CoordinateComponent> = ComponentMapper.getFor(CoordinateComponent::class.java)
  val square: ComponentMapper<SquareComponent> = ComponentMapper.getFor(SquareComponent::class.java)
  val room: ComponentMapper<RoomComponent> = ComponentMapper.getFor(RoomComponent::class.java)
  val creature: ComponentMapper<CreatureComponent> = ComponentMapper.getFor(CreatureComponent::class.java)
}
