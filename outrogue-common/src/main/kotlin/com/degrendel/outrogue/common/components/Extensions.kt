package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.Entity

// Mapper helper functions
fun Entity.getCoordinate() = ComponentMaps.coordinate.get(this).coordinate
fun Entity.getSquare() = ComponentMaps.square.get(this).square
fun Entity.getRoomData() = ComponentMaps.roomData.get(this)!!
fun Entity.getCreature() = ComponentMaps.creature.get(this).creature
fun Entity.getAllegiance() = ComponentMaps.allegiance.get(this).allegiance

fun Entity.isWithinThisRoom(x: Int, y: Int): Boolean
{
  val topLeft = this.getCoordinate()
  val dimensions = this.getRoomData()
  return (x >= topLeft.x
      && x < topLeft.x + dimensions.width
      && y >= topLeft.y
      && y < topLeft.y + dimensions.height)
}
