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

  override fun getAllMoves(coordinate: Coordinate, filter: (candidate: Square, cost: Int, baseCost: Int) -> Boolean): Map<EightWay, Pair<Coordinate, Int>>
  {
    val baseCost = data[coordinate.x][coordinate.y]
    return EightWay.values().map { Pair(it, coordinate.move(it)) }
        .filter { (_, candidate) -> candidate.isValid() && coordinate.canInteract(world, candidate) }
        .filter { (_, candidate) ->
          filter(world.getSquare(candidate), data[candidate.x][candidate.y], baseCost)
        }
        .map { (direction, coordinate) -> Pair(direction, Pair(coordinate, data[coordinate.x][coordinate.y])) }
        .toMap()
  }

  override fun getBestMoves(coordinate: Coordinate, filter: (candidate: Square, cost: Int, baseCost: Int) -> Boolean): List<Pair<EightWay, Coordinate>> =
    getAllMoves(coordinate, filter).let { candidates ->
      var bestCost = Int.MAX_VALUE
      candidates.map { (_, data) -> data.second }.forEach { if (it < bestCost) bestCost = it }
      // TODO: This might be an error - log it?
      if (bestCost == Int.MAX_VALUE)
        listOf()
      else
        candidates.filter { (_, data) -> data.second == bestCost }
            .map { (eightWay, data) -> Pair(eightWay, data.first) }
            .shuffled(random)
    }

  override fun getBestMove(coordinate: Coordinate, filter: (candidate: Square, cost: Int, baseCost: Int) -> Boolean): Pair<EightWay, Coordinate>?
  {
    return getBestMoves(coordinate, filter).firstOrNull()
  }
}