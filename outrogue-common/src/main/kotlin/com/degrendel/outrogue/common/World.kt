package com.degrendel.outrogue.common

interface World
{
  val rogue: Creature
  val conjurer: Creature

  fun getLevel(floor: Int): Level
}