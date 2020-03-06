package com.degrendel.outrogue.common.world

import com.badlogic.ashley.core.Entity

interface Room
{
  val id: Int
  val entity: Entity
  val topLeft: Coordinate
  val width: Int
  val height: Int

  val interior: Set<Coordinate>
  val walkable: Set<Coordinate>
  val border: Set<Coordinate>
  val entire: Set<Coordinate>

  fun isWithin(x: Int, y: Int): Boolean
}