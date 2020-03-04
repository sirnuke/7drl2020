package com.degrendel.outrogue.common.world

import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.creatures.Rogue

interface World
{
  val rogue: Rogue
  val conjurer: Creature

  fun getLevel(coordinate: Coordinate): Level
  fun getLevel(floor: Int): Level

  fun getSquare(coordinate: Coordinate): Square
  fun getSquare(x: Int, y: Int, floor: Int): Square

  fun canMoveCheckingCreatures(from: Coordinate, direction: EightWay): Boolean
  fun canMoveIgnoringCreatures(from: Coordinate, direction: EightWay): Boolean
  fun canMoveCheckingCreatures(from: Coordinate, to: Coordinate): Boolean
  fun canMoveIgnoringCreatures(from: Coordinate, to: Coordinate): Boolean
}