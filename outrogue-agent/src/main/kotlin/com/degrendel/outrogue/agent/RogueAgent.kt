package com.degrendel.outrogue.agent

import ch.qos.logback.classic.Level
import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Agent
import org.drools.core.event.DebugRuleRuntimeEventListener
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession
import java.io.File
import ch.qos.logback.classic.Logger
import com.degrendel.outrogue.agent.goals.DecideAction
import com.degrendel.outrogue.agent.inputs.AutoClean
import com.degrendel.outrogue.agent.inputs.ExploreOption
import com.degrendel.outrogue.common.agent.Sleep
import org.kie.api.runtime.rule.FactHandle
import org.slf4j.LoggerFactory

class RogueAgent(val engine: Engine) : Agent
{
  companion object
  {
    private val L = LoggerFactory.getLogger(RogueAgent::class.java) as Logger
    // NOTE: getResource wants preceding slash but Drools - in its infinite wisdom - doesn't >:[
    const val JAVA_PATH = "/com/degrendel/outrogue/agent/rules"
    const val DROOLS_PATH = "com/degrendel/outrogue/agent/rules"
  }

  private val session: KieSession

  init
  {
    // Ick.  Thanks drools.
    val services = KieServices.Factory.get()
    val filesystem = services.newKieFileSystem()
    File(RogueAgent::class.java.getResource(JAVA_PATH).path).list()!!.forEach {
      L.info("Source Drools rulefile drools/{}", it)
      filesystem.write(services.resources.newClassPathResource("$DROOLS_PATH/$it"))
    }
    val module = services.newKieBuilder(filesystem).buildAll().kieModule
    val container = services.newKieContainer(module.releaseId)
    session = container.newKieSession()

    session.setGlobal("agent", this)
    session.setGlobal("L", L)
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

    rogue.computeExploreDirection()?.let { session.insert(ExploreOption(it)) }

    val rootGoal = DecideAction()
    session.insert(rootGoal)
    session.fireAllRules()
    val actions = session.getObjects { it is Action }.map { it as Action }
    session.getFactHandles<FactHandle> { it is AutoClean }.forEach { session.delete(it) }
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