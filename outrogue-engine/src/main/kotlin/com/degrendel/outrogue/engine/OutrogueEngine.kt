package com.degrendel.outrogue.engine

import com.degrendel.outrogue.agent.RogueAgent
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.agent.*
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.creatures.Allegiance
import com.degrendel.outrogue.common.world.SquareType
import com.degrendel.outrogue.common.world.World
import com.degrendel.outrogue.common.world.creatures.Creature
import com.github.czyzby.noise4j.map.generator.util.Generators
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.asJavaRandom

class OutrogueEngine(val frontend: Frontend, overrideSeed: Long?) : Engine
{
  companion object
  {
    private val L by logger()
  }

  override val seed = overrideSeed ?: Random.nextLong()

  private var _clock = 0L
  override val clock get() = _clock

  override val random: Random = Random(seed)
  override val ecs = ECS()

  val _rogueAgent = RogueAgent(this)

  override val rogueAgent: Agent get() = _rogueAgent

  private val _world = WorldState(this)
  override val world: World get() = _world

  private val actionQueue = ActionQueue(this)


  init
  {
    L.info("Creating engine with seed {}", seed)
    Generators.setRandom(random.asJavaRandom())
  }

  override fun bootstrapECS()
  {
    L.info("Bootstrapping ECS")
    _world.bootstrapECS()
    updateECS()
  }

  private fun updateECS()
  {
    L.trace("Updating ECS")
    ecs.update(0.0f)
  }

  override fun computeCost(action: Action): Long
  {
    return when (action)
    {
      is Sleep -> P.costs.sleep
      // TODO: Charge more for diagonal?
      is Move -> P.costs.move
      is GoDownStaircase, is GoUpStaircase -> P.costs.staircase
      is MeleeAttack -> P.costs.melee
      is ProdCreature -> P.costs.prod
    }
  }

  override fun runGame(): Job = GlobalScope.launch {
    // TODO: If we need a lot more performance with drawing, this /should/ work as expected.
    //      The frontend accesses the levels, but the actual modifications shouldn't impact it?
    //      It visibly desyncs the drawing from the turn execution, however
    /*
    launch {
      while (true)
      {
        frontend.refreshMap()
        delay(100L)
      }
    }
     */
    while (this.isActive)
    {
      _world.computeVisibleAndKnown()
      updateECS()
      frontend.refreshMap()
      executeNextAction()
      // TODO: Alternatively for performance, offer a 'peak ahead' in actionQueueSystem.  If the next action is a
      //       simple AI (i.e. should be near immediately), skip refreshing the map.  Could also have a timer that
      //       asserts it hasn't been too long.
    }
  }

  // TODO: Return a message alongside this to report to the player
  override fun isValidAction(action: Action): Boolean
  {
    val level = _world.getLevel(action.creature.coordinate)

    return when (action)
    {
      is Sleep -> true
      is Move -> world.canMoveCheckingCreatures(action.creature.coordinate, action.direction)
      is GoUpStaircase ->
      {
        // Only rogues and their allys can leave the dungeon
        if (level.isFirst && action.creature.allegiance != Allegiance.ROGUE)
          false
        else
        {
          val square = level.getSquare(action.creature.coordinate)
          when
          {
            // Not a staircase? BE GONE
            square.type != SquareType.STAIRCASE_UP -> false
            // If we've passed the team check, then can always leave the dungeon
            level.isFirst -> true
            // Otherwise, check if the landing is clear
            else ->
              world.getLevel(action.creature.coordinate.floor - 1).staircasesDown[square.staircase!!].creature == null
          }
        }
      }
      is GoDownStaircase ->
      {
        val square = level.getSquare(action.creature.coordinate)
        when
        {
          square.type != SquareType.STAIRCASE_DOWN -> false
          else ->
            world.getLevel(action.creature.coordinate.floor + 1).staircasesUp[square.staircase!!].creature == null
        }
      }
      // TODO: Ick
      is ProdCreature ->
      {
        if (action.target !is MinionState)
          false
        else if (!action.creature.coordinate.canInteract(world, action.target.coordinate))
          false
        else if (action.creature.allegiance == Allegiance.ROGUE && action.target.allegiance == Allegiance.CONJURER)
          false
        else !(action.creature.allegiance == Allegiance.CONJURER && action.target.allegiance == Allegiance.ROGUE)
      }
      is MeleeAttack ->
      {
        if (!action.creature.coordinate.canInteract(world, action.target.coordinate))
          false
        else if (action.creature.allegiance == action.target.allegiance)
          false
        else if (action.creature.allegiance == Allegiance.NEUTRAL)
          false
        else action.target.allegiance != Allegiance.NEUTRAL
      }
    }
  }

  private suspend fun executeNextAction() = applyAction(actionQueue.execute())

  fun updateClock(cooldown: Long, creature: CreatureState)
  {
    assert(_clock <= creature.clock)
    _clock = creature.clock
    creature.addCooldown(cooldown)
    L.debug("Applied cooldown {} to {}, clock is now {}", cooldown, creature, _clock)
  }

  private fun applyAction(action: Action) = when (action)
  {
    is Sleep -> L.debug("Creature {} sleeps", action.creature)
    is Move -> _world.getLevelState(action.creature).move(action.creature as CreatureState, action.direction)
    is GoDownStaircase -> _world.navigateStaircase(action.creature as CreatureState, descending = true)
    is GoUpStaircase -> _world.navigateStaircase(action.creature as CreatureState, descending = false)
    is MeleeAttack -> _world.performMeleeAttack(action.creature as CreatureState, action.target as CreatureState)
    is ProdCreature -> (action.target as MinionState).prod(clock)
  }

  override fun contextualAction(creature: Creature, eightWay: EightWay): Action?
  {
    // Can't move even when ignoring creatures? bad direction/into wall/etc
    if (!world.canMoveIgnoringCreatures(creature.coordinate, eightWay)) return null
    val coordinate = creature.coordinate.move(eightWay)
    val target = world.getSquare(coordinate)
    // Nothing at the target? Move to action
    if (target.creature == null)
      return Move(creature, eightWay)
    val other = target.creature!!
    // Team Rogue in the way?  Attack!
    if (other.allegiance == Allegiance.ROGUE)
      return MeleeAttack(creature, other)
    // Else, prod the blocking creature
    return ProdCreature(creature, other)
  }
}