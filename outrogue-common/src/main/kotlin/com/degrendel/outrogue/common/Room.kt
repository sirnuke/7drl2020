package com.degrendel.outrogue.common

import com.badlogic.ashley.core.Entity

interface Room
{
  val id: Int
  val entity: Entity
  val topLeft: Coordinate
  val width: Int
  val height: Int

  fun isWithin(x: Int, y: Int): Boolean
}