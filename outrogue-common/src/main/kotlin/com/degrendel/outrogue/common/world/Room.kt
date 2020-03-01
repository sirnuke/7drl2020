package com.degrendel.outrogue.common.world

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.world.Coordinate

interface Room
{
  val id: Int
  val entity: Entity
  val topLeft: Coordinate
  val width: Int
  val height: Int

  fun isWithin(x: Int, y: Int): Boolean
}