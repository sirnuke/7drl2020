package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Level

interface NavigationMap
{
  fun compute(sources: Map<Coordinate, Int>, skip: Set<Coordinate>)
  fun getBestMove(coordinate: Coordinate): EightWay?
}