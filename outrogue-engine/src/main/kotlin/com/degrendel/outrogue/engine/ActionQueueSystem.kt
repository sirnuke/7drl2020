package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.ai.AgentController
import com.degrendel.outrogue.common.ai.PlayerController
import com.degrendel.outrogue.common.ai.SimpleController
import com.degrendel.outrogue.common.components.getCreature
import java.util.*

class ActionQueueSystem(private val world: WorldState) : EntityListener
{
  companion object
  {
    private val L by logger()
  }

  private val queue = PriorityQueue<Entity>(10, Comparator<Entity> { e1, e2 ->
    val c1 = e1.getCreature()
    val c2 = e2.getCreature()
    assert(c1 != c2)
    assert(c1.id != c2.id)
    if (c1.cooldown == c2.cooldown)
      c1.id - c2.id
    else
      (c1.cooldown - c2.cooldown).toInt()
  })

  override fun entityAdded(entity: Entity)
  {
    L.info("Adding entity {} to the action queue", entity)
    queue.add(entity)
  }

  override fun entityRemoved(entity: Entity)
  {
    queue.remove(entity)
  }

  suspend fun execute(): Action
  {
    val entity = queue.poll()
    val creature = entity.getCreature()
    L.debug("Executing turn for {}, {}", entity, creature)
    val action = when (creature.controller)
    {
      is PlayerController -> TODO("Not yet implemented")
      is AgentController -> TODO("Agent isn't supported yet")
      is SimpleController -> TODO("Not yet implemented")
    }
    // TODO: Compute cost of performing action, put entity back into queue
    // creature.cooldown += world.computeCost(action)
    // queue.add(entity)
    return action
  }
}