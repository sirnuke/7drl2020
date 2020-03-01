package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.Coordinate

// Mapper helper functions
fun Entity.getCoordinate() = ComponentMaps.coordinate.get(this).coordinate
fun Entity.getSquare() = ComponentMaps.square.get(this).square
fun Entity.getRoomData() = ComponentMaps.roomData.get(this)!!
fun Entity.getCreature() = ComponentMaps.creature.get(this).creature
fun Entity.getAllegiance() = ComponentMaps.allegiance.get(this).allegiance

fun Entity.isWithinThisRoom(coordinate: Coordinate): Boolean
{
  val topLeft = this.getCoordinate()
  val dimensions = this.getRoomData()
  return (coordinate.x >= topLeft.x
      && coordinate.x < topLeft.x + dimensions.width
      && coordinate.y >= topLeft.y
      && coordinate.y < topLeft.y + dimensions.height)
}
