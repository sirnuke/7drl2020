package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Level

interface NavigationMap
{
  val data: List<List<Int>>
  fun compute(sources: Map<Coordinate, Int>, skip: Set<Coordinate> = setOf(), terminate: Set<Coordinate> = setOf()): NavigationMap
  fun getBestMove(coordinate: Coordinate): EightWay?
}