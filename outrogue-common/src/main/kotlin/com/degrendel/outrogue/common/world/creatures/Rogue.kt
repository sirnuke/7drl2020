package com.degrendel.outrogue.common.world.creatures

import com.degrendel.outrogue.common.world.EightWay

interface Rogue : Creature
{
  fun computeExploreDirection(): EightWay?
}