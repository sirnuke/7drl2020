package com.degrendel.outrogue.engine

import com.degrendel.outrogue.agent.RogueSoarAgent
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.ai.Move
import com.degrendel.outrogue.common.ai.Sleep
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.World
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


  override val random: Random = Random(seed)
  override val ecs = ECS()

  private val soarAgent = RogueSoarAgent()

  private val _world = WorldState(this)
  override val world: World get() = _world

  private val actionQueue = ActionQueue(this)


  init
  {
    L.info("Creating engine with seed {}", seed)
    Generators.setRandom(random.asJavaRandom())
  }

  override fun openAgentDebuggers()
  {
    soarAgent.openDebugger()
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
    }
  }

  override fun runGame(): Job = GlobalScope.launch {
    // TODO: If we need a lot more performance with drawing, this /should/ work as expected
    //      the frontend access the levels, but the actual modifications shouldn't impact it?
    //      It desyncs the drawing from the turn execution, which is visible noticable, however
    /*
    launch {
      while (true)
      {
        frontend.refreshMap()
        delay(100L)
      }
    }
     */
    frontend.refreshMap()
    while (this.isActive)
    {
      updateECS()
      val action = actionQueue.execute()
      executeAction(action)
      // TODO: Alternatively for performance, offer a 'peak ahead' in actionQueueSystem.  If the next action is a
      //       simple AI (i.e. should be near immediately), skip refreshing the map.  Could also have a timer that
      //       asserts it hasn't been too long.
      frontend.refreshMap()
    }
  }

  override fun isValidAction(action: Action) = when (action)
  {
    is Sleep -> true
    is Move -> _world.getLevel(action.creature).canMove(action.creature.coordinate, action.direction)
  }

  private fun executeAction(action: Action) = when (action)
  {
    is Sleep -> L.debug("Creature {} sleeps", action.creature)
    is Move -> _world.getLevel(action.creature).move(action.creature as CreatureState, action.direction)
  }
}