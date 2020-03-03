package com.degrendel.outrogue.common.world

import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.creatures.Rogue

interface World
{
  val rogue: Rogue
  val conjurer: Creature

  fun getLevel(floor: Int): Level

  fun getSquare(coordinate: Coordinate): Square
}