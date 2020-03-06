package com.degrendel.outrogue.common.world.creatures

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay

interface Rogue : Creature
{
  fun computeExploreDirection(): Map<EightWay, Pair<Coordinate, Int>>
}