package com.degrendel.outrogue.agent

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Agent
import com.degrendel.outrogue.common.logger
import org.kie.api.KieServices
import org.kie.api.runtime.KieSession
import java.io.File

class RogueAgent(val engine: Engine) : Agent
{
  companion object
  {
    private val L by logger()
  }

  private val session: KieSession

  init
  {
    // Ick.  Thanks drools.
    val services = KieServices.Factory.get()
    val filesystem = services.newKieFileSystem()
    // NOTE: getResource wants /drools, but Drools - in its infinite wisdom, wants drools/.  Meh
    File(RogueAgent::class.java.getResource("/drools").path).list()!!.forEach {
      L.info("Source Drools rulefile drools/{}", it)
      filesystem.write(services.resources.newClassPathResource("drools/$it"))
    }
    val module = services.newKieBuilder(filesystem).buildAll().kieModule
    val container = services.newKieContainer(module.releaseId)
    session = container.newKieSession()
  }

  override fun enableDebugging()
  {
    // TODO: Enable drools logging
  }

  override suspend fun requestAction(): Action
  {
    TODO("Stub!")
  }
}