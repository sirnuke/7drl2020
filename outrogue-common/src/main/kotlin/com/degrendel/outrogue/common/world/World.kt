package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.world.Creature
import com.degrendel.outrogue.common.world.Level

interface World
{
  val rogue: Creature
  val conjurer: Creature

  fun getLevel(floor: Int): Level

  fun getSquare(coordinate: Coordinate): Square
}