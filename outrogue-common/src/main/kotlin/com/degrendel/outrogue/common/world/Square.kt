package com.degrendel.outrogue.common.world

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.properties.Properties

interface Square
{
  val type: SquareType
  val coordinate: Coordinate
  val entity: Entity
  val wallOrientation: WallOrientation
  val room: Int?
  val visible: Set<Int>

  fun isNavigable(): Boolean

  companion object
  {
    val xRange = (0 until Properties.P.map.width)
    val yRange = (0 until Properties.P.map.height)
    val all: List<Coordinate>

    init
    {
      val _all = mutableListOf<Coordinate>()
      each { x, y -> _all += Coordinate(x, y, 0) }
      all = _all
    }

    fun each(lambda: (x: Int, y: Int) -> Unit) =
      xRange.forEach { x -> yRange.forEach { y -> lambda(x, y) } }

    fun each(lambda: (coordinate: Coordinate) -> Unit) = all.forEach(lambda)
  }
}

enum class SquareType(val blocked: Boolean, val roomBorder: Boolean)
{
  BLOCKED(true, false),
  CORRIDOR(false, false),
  WALL(true, true),
  FLOOR(false, false),
  DOOR(false, true),
  ;
}

enum class Cardinal(val x: Int, val y: Int)
{
  NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0)
}

enum class EightWay(val x: Int, val y: Int, val diagonal: Boolean)
{
  NORTH(0, -1, false),
  NORTH_EAST(1, -1, true),
  EAST(1, 0, false),
  SOUTH_EAST(1, 1, true),
  SOUTH(0, 1, false),
  SOUTH_WEST(-1, 1, true),
  WEST(-1, 0, false),
  NORTH_WEST(-1, -1, true),
  ;

  val diagonalChecks: List<Pair<Int, Int>> = if (!diagonal)
    listOf()
  else
    listOf(Pair(0, y), Pair(x, 0))

}

enum class WallOrientation
{
  NONE,               //     ain't no wall
  NORTH_SOUTH,        //  |  vertical
  EAST_WEST,          // --  horizontal
  NORTH_EAST,         // ^>  corner upper right
  EAST_SOUTH,         // v>  corner lower right
  SOUTH_WEST,         // <v  corner lower left
  WEST_NORTH,         // <^  corner upper left
  NORTH_EAST_SOUTH,   // |>  vertical plus right
  EAST_SOUTH_WEST,    // --v horizontal plus down
  SOUTH_WEST_NORTH,   // <|  vertical plus left
  WEST_NORTH_EAST,    // ^-- horizontal plus up
  ALL,                // + all four (rare)
  ;

  companion object
  {
    // Ick.  I'm guessing there's some math function to do this, but whatever
    // Could do this a bit more efficiently with bitmasks, but this lookup should still be fairly quick, and only
    // done once per level generation.  Compared to the memory hog that is Soar, probably not that expensive to just
    // keep them in memory for when the rogue starts going back up the stairs.
    val lookup = mapOf(
        setOf<Cardinal>() to NONE,
        setOf(Cardinal.NORTH) to NORTH_SOUTH,
        setOf(Cardinal.SOUTH) to NORTH_SOUTH,
        setOf(Cardinal.NORTH, Cardinal.SOUTH) to NORTH_SOUTH,
        setOf(Cardinal.EAST) to EAST_WEST,
        setOf(Cardinal.WEST) to EAST_WEST,
        setOf(Cardinal.EAST, Cardinal.WEST) to EAST_WEST,
        setOf(Cardinal.NORTH, Cardinal.EAST) to NORTH_EAST,
        setOf(Cardinal.EAST, Cardinal.SOUTH) to EAST_SOUTH,
        setOf(Cardinal.SOUTH, Cardinal.WEST) to SOUTH_WEST,
        setOf(Cardinal.WEST, Cardinal.NORTH) to WEST_NORTH,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH) to NORTH_EAST_SOUTH,
        setOf(Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to EAST_SOUTH_WEST,
        setOf(Cardinal.SOUTH, Cardinal.WEST, Cardinal.NORTH) to SOUTH_WEST_NORTH,
        setOf(Cardinal.WEST, Cardinal.NORTH, Cardinal.EAST) to WEST_NORTH_EAST,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to ALL
    )
  }
}