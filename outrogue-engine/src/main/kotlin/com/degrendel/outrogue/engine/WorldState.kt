package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.Level.Companion.floorRange

class WorldState(val engine: OutrogueEngine) : World
{
  private val levels: List<LevelState>

  init
  {
    // TODO: Ick
    var previous: LevelState? = null
    levels = floorRange.map { previous = LevelState(it, previous, engine); previous!! }
  }

  val rogueTeam = Family.all(CreatureComponent::class.java, RogueAllegianceComponent::class.java).get()

  val creaturesVisibleToRogue = Family.all(CreatureComponent::class.java, VisibleToRogueComponent::class.java).get()
  val squaresVisibleToRogue = Family.all(SquareComponent::class.java, VisibleToRogueComponent::class.java).get()

  private val _conjurer: Conjurer
  private var _rogue: Rogue

  init
  {
    levels[0].let { level ->
      level.getRandomRooms(2).let { rooms ->
        assert(rooms.size == 2)
        // TODO: Filter to avoid staircases
        _conjurer = Conjurer(Entity(), rooms[0].getRandomSquare { true }!!, 0L)
            .also { level.spawn(it) }
        _rogue = Rogue(Entity(), rooms[1].getRandomSquare { true }!!, 0L)
            .also { level.spawn(it) }
      }
    }

    // TODO: Tell levels to spawn initial creatures, items, etc
  }

  override val conjurer: Creature get() = _conjurer
  override val rogue: Creature get() = _rogue

  override fun getLevel(floor: Int): Level = levels[floor]

  override fun getSquare(coordinate: Coordinate) = levels[coordinate.floor].getSquare(coordinate)

  fun getLevel(creature: Creature) = levels[creature.coordinate.floor]

  fun bootstrapECS()
  {
    levels.forEach { it.bootstrapECS(engine.ecs, conjurer.coordinate.floor) }
  }

  fun setVisibleFloor(floor: Int)
  {
    levels.forEach { it.setOnVisibleLevel(floor == it.floor) }
  }

  // TODO: These functions are nearly identical
  fun goDownStaircase(creature: CreatureState)
  {
    val currentLevel = getLevel(creature)
    val staircase = currentLevel.getSquare(creature.coordinate).staircase!!
    val newLevel = levels[creature.coordinate.floor + 1]
    val landing = newLevel.staircasesUp[staircase].coordinate
    currentLevel.despawn(creature)
    creature.move(landing)
    newLevel.spawn(creature)
    if (creature == conjurer)
      setVisibleFloor(newLevel.floor)
  }

  fun goUpStaircase(creature: CreatureState)
  {
    val currentLevel = getLevel(creature)
    val staircase = currentLevel.getSquare(creature.coordinate).staircase!!
    if (currentLevel.isFirst)
      TODO("Need to implement leaving the dungeon")
    val newLevel = levels[creature.coordinate.floor - 1]
    val landing = newLevel.staircasesDown[staircase].coordinate
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
    engine.ecs.getEntitiesFor(rogueTeam).forEach {
      it.getCreature().coordinate.let { coordinate ->
        rooms.addAll(getSquare(coordinate).visible.map { id -> Pair(coordinate.floor, id) })
        visible.add(coordinate)
        visible.addAll(coordinate.eightWayNeighbors())
      }
    }
    // Iterate through the visible rooms, add them to visible
    rooms.forEach { (floor, id) ->
      levels[floor].getRoom(id).forEach { visible.add(it) }
    }
    // Iterate through all visible, if not in the set remove visible component
    // (Convert asSequence.asIterable to duplicate the array since it messes with the iterators otherwise :/
    engine.ecs.getEntitiesFor(squaresVisibleToRogue).asSequence().asIterable().forEach {
      if (it.getCoordinate() !in visible)
        it.remove(VisibleToRogueComponent::class.java)
    }
    // For each currently visible thing, if not in set<Coordinate> remove visible component
    engine.ecs.getEntitiesFor(creaturesVisibleToRogue).asSequence().asIterable().forEach {
      if (it.getCoordinate() !in visible)
        it.remove(VisibleToRogueComponent::class.java)
    }
    // Mark each coordinate as visible and known.  If there's a creature there, also mark it as visible
    // TODO: Will need to do this with things as well
    visible.forEach {
      levels[it.floor].getSquareState(it).let { square ->
        square.entity.add(VisibleToRogueComponent).add(KnownToRogueComponent)
        square.creature?.entity?.let { creature ->
          creature.add(VisibleToRogueComponent).add(KnownToRogueComponent)
        }
      }
    }
  }
}