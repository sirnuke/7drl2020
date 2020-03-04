package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.NavigationMap
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.Square.Companion.xRange
import com.degrendel.outrogue.common.world.Square.Companion.yRange
import java.util.*
import kotlin.random.Random

class NavigationMapImpl(private val random: Random, private val world: World,
                        private val filterSquares: (Square) -> Boolean = { it.creature == null && !it.type.blocked }) : NavigationMap
{
  companion object
  {
    private val L by logger()
  }

  private val _data: MutableList<MutableList<Int>> = xRange.map { yRange.map { Int.MAX_VALUE }.toMutableList() }.toMutableList()
  override val data: List<List<Int>> get() = _data

  override fun compute(sources: Map<Coordinate, Int>, skip: Set<Coordinate>, terminate: Set<Coordinate>): NavigationMap
  {
    L.trace("Computing navigation map for sources {}, skip {}, terminate {}", sources, skip, terminate)
    Square.each { x, y -> _data[x][y] = Int.MAX_VALUE }
    sources.forEach { (coordinate, cost) -> _data[coordinate.x][coordinate.y] = cost }
    // TODO: This needs to be a priority queue ordered by the last cost
    val toCheck = LinkedList<Coordinate>().also { it.addAll(sources.keys) }
    while (toCheck.isNotEmpty())
    {
      // We functional now (kinda)
      // TODO: This assumes there is no diagonal cost.  If that changes, this will probably need to be a float
      val checking = toCheck.removeFirst().also { if (it in terminate) return this }
      val cost = _data[checking.x][checking.y] + 1
      getNeighbors(checking, skip).forEach {
        _data[it.x][it.y] = cost
        toCheck.addLast(it)
      }
    }
    return this
  }

  private fun getNeighbors(coordinate: Coordinate, skip: Set<Coordinate>) = EightWay.values()
      .filter { world.canMoveIgnoringCreatures(coordinate, it) }
      .map { coordinate.move(it) }
      .filter { it.isValid() }
      .filter { filterSquares(world.getSquare(it)) }
      .filter { _data[it.x][it.y] == Int.MAX_VALUE && it !in skip }

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
      // Can't move due to monster? Skip
      else if (!world.canMoveCheckingCreatures(coordinate, it))
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
      hasStraight -> lowest.shuffled(random).first { !it.diagonal }
      else -> lowest.shuffled(random).first()
    }
  }
}