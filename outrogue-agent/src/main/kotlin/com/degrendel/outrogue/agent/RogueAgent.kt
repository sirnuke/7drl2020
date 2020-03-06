package com.degrendel.outrogue.agent

import ch.qos.logback.classic.Level
import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Agent
import org.drools.core.event.DebugRuleRuntimeEventListener
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession
import ch.qos.logback.classic.Logger
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.degrendel.outrogue.agent.goals.DecideAction
import com.degrendel.outrogue.agent.inputs.AutoClean
import com.degrendel.outrogue.agent.inputs.ExploreOption
import com.degrendel.outrogue.agent.inputs.toInput
import com.degrendel.outrogue.common.agent.Sleep
import com.degrendel.outrogue.common.components.getCreature
import org.kie.api.runtime.rule.FactHandle
import org.slf4j.LoggerFactory

class RogueAgent(val engine: Engine) : Agent
{
  companion object
  {
    private val L = LoggerFactory.getLogger(RogueAgent::class.java) as Logger
    const val DROOLS_PATH = "/com/degrendel/outrogue/agent/rules"

    // TODO: It would be nice to replace this with a walkDir, but not clear how to do this super reliably.  Original
    //       approach was clunky but worked fine in normal builds, but broke in shadowJar release.  Since this is a
    //       known list and fairly small and stable, just hardcode them and be done with it (for now).
    val ruleFiles = arrayOf(
        "explore.drl",
        "rogue.drl",
        "sleep.drl"
    )
  }

  private val session: KieSession

  private val creatureInputs = mutableMapOf<Int, FactHandle>()

  init
  {
    // Ick.  Thanks drools.
    val services = KieServices.Factory.get()
    val filesystem = services.newKieFileSystem()
    ruleFiles.forEach {
      val jarPath = "$DROOLS_PATH/$it"
      val droolsPath = "src/main/resources$DROOLS_PATH/$it"
      L.info("Sourcing Drools rule file from {} into {}", jarPath, droolsPath)
      val stream = RogueAgent::class.java.getResourceAsStream(jarPath)
      filesystem.write(droolsPath, services.resources.newInputStreamResource(stream))
      stream.close()
    }
    val module = services.newKieBuilder(filesystem).buildAll().kieModule
    val container = services.newKieContainer(module.releaseId)
    session = container.newKieSession()

    session.setGlobal("agent", this)
    session.setGlobal("L", L)

    engine.ecs.addEntityListener(engine.creaturesKnownToRogue, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        val creature = entity.getCreature().toInput(engine.world)
        assert(creature.id !in creatureInputs)
        creatureInputs[creature.id] = session.insert(creature)
      }

      override fun entityRemoved(entity: Entity)
      {
        session.delete(creatureInputs.getValue(entity.getCreature().id))
      }
    })
  }

  override fun enableDebugging()
  {
    session.addEventListener(DebugRuleRuntimeEventListener())
  }

  override fun enableLogging()
  {
    L.level = Level.DEBUG
  }

  override suspend fun requestAction(): Action
  {
    L.info("Requesting action...")
    val rogue = engine.world.rogue
    session.setGlobal("creature", rogue)
    session.setGlobal("level", engine.world.getLevel(rogue.coordinate.floor))

    engine.ecs.getEntitiesFor(engine.creaturesKnownToRogue)
        .map { it.getCreature().toInput(engine.world) }
        .forEach { session.update(creatureInputs.getValue(it.id), it) }

    rogue.computeExploreDirection()?.let { session.insert(ExploreOption(it)) }
    EightWay.values().map { Pair(it, rogue.coordinate.move(it)) }
        .filter { it.second.isValid() }
        .map { (direction, coordinate) -> Pair(direction, engine.world.getSquare(coordinate)) }
        .map { (direction, square) -> Neighbor(direction, square, square.creature) }
        .forEach { session.insert(it) }

    val rootGoal = DecideAction()
    session.insert(rootGoal)
    session.fireAllRules()
    val actions = session.getObjects { it is Action }.map { it as Action }
    session.getFactHandles<FactHandle> { it is AutoClean || it is Action }.forEach { session.delete(it) }
    if (!rootGoal.accomplished)
      L.warn("Unable to accomplish decide action goal!")
    return when
    {
      actions.size > 1 ->
      {
        L.warn("Rogue agent returned multiple actions, choosing one at random!")
        actions.shuffled(engine.random).first()
      }
      actions.isEmpty() ->
      {
        L.warn("Rogue agent returned zero actions, sleeping")
        Sleep(engine.world.rogue)
      }
      else -> actions.first()
    }
  }
}