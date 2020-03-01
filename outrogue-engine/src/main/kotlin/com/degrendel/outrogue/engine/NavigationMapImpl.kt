package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.Coordinate
import com.degrendel.outrogue.common.EightWay
import com.degrendel.outrogue.common.Level
import com.degrendel.outrogue.common.NavigationMap
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import java.util.*

class NavigationMapImpl(level: Level, sources: List<Coordinate>, skip: Set<Coordinate>) : NavigationMap
{
  private val _data: MutableList<MutableList<Int>> = (0 until P.map.width).map { (0 until P.map.height).map { Int.MAX_VALUE }.toMutableList() }.toMutableList()
  val data: List<List<Int>> get() = _data

  init
  {
    compute(level, sources, skip)
  }

  override fun compute(level: Level, sources: List<Coordinate>, skip: Set<Coordinate>)
  {
    (0 until P.map.width).forEach { x -> (0 until P.map.height).forEach { y -> _data[x][y] = Int.MAX_VALUE } }
    sources.forEach { _data[it.x][it.y] = 0 }
    val toCheck = LinkedList<Coordinate>().also { it.addAll(sources) }
    while (toCheck.isNotEmpty())
    {
      // We functional now (kinda)
      // TODO: This assumes there is no diagonal cost.  If that changes, this will probably need to be a float
      val cost: Int
      getNeighbors(level, toCheck.removeFirst().also { cost = _data[it.x][it.y] + 1 }, skip).forEach {
        _data[it.x][it.y] = cost
        toCheck.addLast(it)
      }
    }
  }

  private fun getNeighbors(level: Level, coordinate: Coordinate, skip: Set<Coordinate>) = coordinate
      .eightWayNeighbors()
      .filter { _data[it.x][it.y] == Int.MAX_VALUE }
      .filter { it in skip }
      .filter { level.isNavigable(it) }

  override fun getBestMove(coordinate: Coordinate): EightWay?
  {
    // TODO: This is hilariously disgusting
    var lowest = mutableListOf<EightWay>()
    var cost = Int.MAX_VALUE
    var hasStraight = false
    EightWay.values().forEach {
      // For direction:
      val new = coordinate.move(it)
      // Not valid? Skip
      if (!new.isValid()) return@forEach
      val newCost = data[new.x][new.y]
      // Unreachable or higher cost than current min cost? skip
      if (newCost == Int.MAX_VALUE || newCost > cost)
        return@forEach
      // Equal to current min cost? Add to list
      else if (newCost == cost)
      {
        // But if it's a straight direction, remember this
        if (!it.diagonal) hasStraight = true
        lowest.add(it)
      }
      // Otherwise, must be a new min cost
      else
      {
        cost = newCost
        lowest = mutableListOf<EightWay>(it)
        hasStraight = !it.diagonal
      }
    }

    // And when returning, favor straight directions if the same cost
    return when
    {
      lowest.isEmpty() -> null
      hasStraight -> lowest.shuffled().first { !it.diagonal }
      else -> lowest.shuffled().first()
    }
  }
}