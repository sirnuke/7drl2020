package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.ai.*
import com.degrendel.outrogue.common.components.ActiveComponent
import com.degrendel.outrogue.common.components.CreatureComponent
import com.degrendel.outrogue.common.components.getCreature
import java.util.*

class ActionQueue(private val engine: OutrogueEngine) : EntityListener
{
  companion object
  {
    private val L by logger()
  }

  private val queue = PriorityQueue<CreatureState>(10, Comparator<CreatureState> { c1, c2 ->
    assert(c1 != c2)
    assert(c1.id != c2.id)
    if (c1.cooldown == c2.cooldown)
      c1.id - c2.id
    else
      (c1.cooldown - c2.cooldown).toInt()
  })

  init
  {
    val family = Family.all(CreatureComponent::class.java, ActiveComponent::class.java).get()
    engine.ecs.addEntityListener(family, this)
  }

  override fun entityAdded(entity: Entity)
  {
    L.info("Adding entity {} / {} to the action queue", entity, entity.getCreature())
    queue.add(entity.getCreature() as CreatureState)
  }

  override fun entityRemoved(entity: Entity)
  {
    queue.remove(entity.getCreature() as CreatureState)
  }

  suspend fun execute(): Action
  {
    val creature = queue.poll()
    val controller = creature.controller
    L.debug("Executing turn for {}", creature)
    val action = when (controller)
    {
      is PlayerController -> engine.frontend.getPlayerInput()
      // TODO: Actually implement the soar agent
      is AgentController -> Sleep(creature)
      is SimpleController -> executeSimpleAI(engine, creature, controller)
    }
    L.info("Creature {} will perform {}", creature, action)
    // TODO: Double check the logic here
    creature.addCooldown(engine.computeCost(action))
    queue.add(creature)
    return action
  }
}