package com.degrendel.outrogue.common

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Level
import com.degrendel.outrogue.common.world.Square

interface NavigationMap
{
  val data: List<List<Int>>
  fun compute(sources: Map<Coordinate, Int>, skip: Set<Coordinate> = setOf(), terminate: Set<Coordinate> = setOf()): NavigationMap
  fun getBestMove(coordinate: Coordinate, filter: (candidate: Square, cost: Int, baseCost: Int) -> Boolean): Pair<EightWay, Coordinate>?
  fun getBestMoves(coordinate: Coordinate, filter: (candidate: Square, cost: Int, baseCost: Int) -> Boolean): List<Pair<EightWay, Coordinate>>
}