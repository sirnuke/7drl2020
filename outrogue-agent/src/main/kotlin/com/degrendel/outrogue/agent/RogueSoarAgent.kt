package com.degrendel.outrogue.agent

import com.degrendel.outrogue.common.ai.Agent
import com.degrendel.outrogue.common.logger
import org.jsoar.kernel.SoarException
import org.jsoar.runtime.ThreadedAgent
import org.jsoar.util.commands.SoarCommands
import java.util.concurrent.Callable
import kotlin.system.exitProcess

class RogueSoarAgent : Agent
{
  companion object
  {
    val L by logger()
  }

  private val agent: ThreadedAgent

  init
  {
    System.setProperty("jsoar.agent.interpreter", "tcl")
    agent = ThreadedAgent.create("Rogue")!!
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
}