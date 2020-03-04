package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.Level.Companion.floorRange
import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.creatures.Rogue

class WorldState(val engine: OutrogueEngine) : World
{
  companion object
  {
    private val L by logger()
  }

  private val levels: List<LevelState>

  init
  {
    // TODO: Ick
    var previous: LevelState? = null
    levels = floorRange.map { previous = LevelState(it, previous, engine); previous!! }
  }

  private val _conjurer: ConjurerState
  private var _rogue: RogueState

  init
  {
    levels[0].let { level ->
      level.getRandomRooms(2).let { rooms ->
        assert(rooms.size == 2)
        // TODO: Filter to avoid staircases
        _conjurer = ConjurerState(Entity(), rooms[0].getRandomSquare { true }!!)
            .also { level.spawn(it) }
        _rogue = RogueState(engine, this, Entity(), rooms[1].getRandomSquare { true }!!, 0L)
            .also { level.spawn(it) }
      }
    }

    levels.forEach { it.populate(this) }
  }

  override val conjurer: Creature get() = _conjurer
  override val rogue: Rogue get() = _rogue

  override fun getLevel(floor: Int): Level = levels[floor]
  override fun getLevel(coordinate: Coordinate): Level = levels[coordinate.floor]
  override fun getSquare(coordinate: Coordinate) = levels[coordinate.floor].getSquare(coordinate)
  override fun getSquare(x: Int, y: Int, floor: Int) = levels[floor].getSquare(x, y)

  fun getLevelState(creature: Creature) = levels[creature.coordinate.floor]

  fun bootstrapECS()
  {
    levels.forEach { it.bootstrapECS(engine.ecs, conjurer.coordinate.floor) }
  }

  fun setVisibleFloor(floor: Int)
  {
    levels.forEach { it.setOnVisibleLevel(floor == it.floor) }
  }

  fun navigateStaircase(creature: CreatureState, descending: Boolean)
  {
    val currentLevel = getLevelState(creature)
    val staircase = currentLevel.getSquare(creature.coordinate).staircase!!
    if (!descending && currentLevel.isFirst)
      TODO("Need to implement leaving the dungeon")
    val newLevel = if (descending)
      levels[creature.coordinate.floor + 1]
    else
      levels[creature.coordinate.floor - 1]
    val landing = if (descending)
      newLevel.staircasesUp[staircase].coordinate
    else
      newLevel.staircasesDown[staircase].coordinate
    currentLevel.despawn(creature)
    creature.move(landing)
    newLevel.spawn(creature)
    if (creature == conjurer)
      setVisibleFloor(newLevel.floor)
  }

  fun computeVisibleAndKnown()
  {
    // TODO: Ick, also this algorithm might be a bit slow - there's potentially a ton of things being touched.
    //       This is especially true if RogueTeam gets huge and spreads out around the map

    // With list of all friends, compute set<Coordinate> of eightway neighbors plus friends
    val visible = mutableSetOf<Coordinate>()
    // Also record what rooms are visible from the tiles of RogueFriends
    val rooms = mutableSetOf<Pair<Int, Int>>()
    engine.ecs.getEntitiesFor(engine.rogueTeam).forEach {
      it.getCreature().coordinate.let { coordinate ->
        rooms.addAll(getSquare(coordinate).visible.map { id -> Pair(coordinate.floor, id) })
        visible.add(coordinate)
        visible.addAll(coordinate.eightWayNeighbors())
      }
    }
    // Iterate through the visible rooms, add them to visible
    rooms.forEach { (floor, id) ->
      levels[floor].getRoomState(id).walkable.forEach { visible.add(it) }
    }
    // Iterate through all visible, if not in the set remove visible component
    // (Convert asSequence.asIterable to duplicate the array since it messes with the iterators otherwise :/
    engine.ecs.getEntitiesFor(engine.squaresVisibleToRogue).asSequence().asIterable().forEach {
      if (it.getCoordinate() !in visible)
        (it.getSquare() as SquareState).setRogueVisible(false)
    }
    // For each currently visible thing, if not in set<Coordinate> remove visible component
    engine.ecs.getEntitiesFor(engine.creaturesVisibleToRogue).asSequence().asIterable().forEach {
      // TODO: Replace this with a helper
      if (it.getCoordinate() !in visible)
        it.remove(VisibleToRogueComponent::class.java)
    }
    // Mark each coordinate as visible and known.  If there's a creature there, also mark it as visible
    // TODO: Will need to do this with things as well
    visible.forEach {
      levels[it.floor].getSquareState(it).let { square ->
        if (square.type.blocked) return@let
        square.setRogueVisible(true)
        // TODO: Replace this with a a helper
        square.creature?.entity?.add(VisibleToRogueComponent)?.add(KnownToRogueComponent)
      }
    }
  }

  override fun canMoveCheckingCreatures(from: Coordinate, direction: EightWay) = canMoveCheckingCreatures(from, from.move(direction))
  override fun canMoveIgnoringCreatures(from: Coordinate, direction: EightWay) = canMoveIgnoringCreatures(from, from.move(direction))
  override fun canMoveCheckingCreatures(from: Coordinate, to: Coordinate) = canMove(from, to, true)
  override fun canMoveIgnoringCreatures(from: Coordinate, to: Coordinate) = canMove(from, to, false)

  private fun canMove(from: Coordinate, to: Coordinate, checkCreatures: Boolean): Boolean
  {
    val square = getSquare(to)
    return (to.isValid()
        && (!checkCreatures || square.creature == null)
        && !square.type.blocked
        && from.canInteract(this, to))
  }

  fun performMeleeAttack(attacker: CreatureState, defender: CreatureState)
  {
    // TODO: Get weapons (or fists) definitions, +/- based on junk, compute whether he hits.  Probably want to return a
    //       string update of some sort?  Probably message enum
    L.warn("TODO: {} attacks {}!", attacker, defender)
  }
}