package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.Entity

// Mapper helper functions
fun Entity.getCoordinate() = ComponentMaps.coordinate.get(this).coordinate
fun Entity.getSquare() = ComponentMaps.square.get(this).square
fun Entity.getRoom() = ComponentMaps.room.get(this).room
fun Entity.getCreature() = ComponentMaps.creature.get(this).creature
fun Entity.getAllegiance() = ComponentMaps.allegiance.get(this).allegiance

