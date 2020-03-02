package com.degrendel.outrogue.agent

import com.degrendel.outrogue.agent.io.ActionOutput
import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.ai.Action
import com.degrendel.outrogue.common.ai.Agent
import com.degrendel.outrogue.common.ai.Sleep
import com.degrendel.outrogue.common.logger
import kotlinx.coroutines.channels.Channel
import org.jsoar.kernel.SoarException
import org.jsoar.kernel.io.beans.SoarBeanOutputContext
import org.jsoar.kernel.io.beans.SoarBeanOutputHandler
import org.jsoar.kernel.io.beans.SoarBeanOutputManager
import org.jsoar.runtime.ThreadedAgent
import org.jsoar.util.commands.SoarCommands
import java.util.concurrent.Callable
import kotlin.system.exitProcess

class RogueSoarAgent(val engine: Engine) : Agent
{
  companion object
  {
    val L by logger()
  }

  private val agent: ThreadedAgent
  private val outputManager: SoarBeanOutputManager

  private val channel = Channel<Action>()

  init
  {
    System.setProperty("jsoar.agent.interpreter", "tcl")
    agent = ThreadedAgent.create("Rogue")!!
    outputManager = SoarBeanOutputManager(agent.events)
    outputManager.registerHandler("action", object : SoarBeanOutputHandler<ActionOutput>()
    {
      override fun handleOutputCommand(context: SoarBeanOutputContext, bean: ActionOutput)
      {
        agent.stop()
        val action = when (bean.type)
        {
          "sleep" -> Sleep(engine.world.rogue)
          else -> TODO("Unhandled or unknown action ${bean.type}")
        }
        channel.offer(action)
      }
    }, ActionOutput::class.java)
    try
    {
      SoarCommands.source(agent.interpreter, javaClass.getResource("/soar/load.soar"))
    }
    catch (e: SoarException)
    {
      L.error("Unable to source the agent", e)
      exitProcess(-1)
    }
  }

  override fun openDebugger()
  {
    agent.openDebuggerAndWait()
    agent.execute(Callable<Unit> {
      agent.interpreter.eval("watch --decisions 0")
    }, null)
  }

  override suspend fun requestAction(): Action
  {
    agent.runForever()
    return channel.receive()
  }
}