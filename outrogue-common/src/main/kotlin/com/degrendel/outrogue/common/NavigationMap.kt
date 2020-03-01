package com.degrendel.outrogue.common

interface NavigationMap
{
  fun compute(level: Level, sources: List<Coordinate>, skip: Set<Coordinate>)
  fun getBestMove(coordinate: Coordinate): EightWay?
}