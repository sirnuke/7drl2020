package com.degrendel.outrogue.frontend.views.fragments

import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import org.hexworks.cobalt.events.api.Subscription
import org.hexworks.zircon.api.ComponentDecorations
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.TrueTypeFontResources
import org.hexworks.zircon.api.application.Application
import org.hexworks.zircon.api.screen.Screen
import java.util.concurrent.LinkedBlockingQueue

class LogFragment(application: Application, private val screen: Screen)
{
  private val logArea = Components.logArea()
      .withSize(P.views.world.log.width, P.views.world.log.height)
      .withPosition(P.views.world.log.x, P.views.world.log.y)
      .withDecorations(ComponentDecorations.box(title = "Log"))
      .withTileset(TrueTypeFontResources.ibmBios(P.window.fontSize))
      .build()

  private val logMessages = LinkedBlockingQueue<LogMessage>()
  private val logUpdateSubscription: Subscription

  init
  {
    screen.addComponent(logArea)
    logUpdateSubscription = application.beforeRender {
      // TODO: Stylaizing would be nice (different colors for different things and entities?)
      ArrayList<LogMessage>().also { logMessages.drainTo(it) }.mapNotNull {
        when (it)
        {
          is AscendStaircaseMessage -> "${it.creature.type.humanName} ascends a staircase to floor ${it.creature.coordinate.floor + 1}"
          is DescendsStaircaseMessage -> "${it.creature.type.humanName} descends a staircase to floor ${it.creature.coordinate.floor + 1}"
          is MeleeMissMessage -> "${it.attacker.type.humanName} misses ${it.target.type.humanName} with their ${it.attacker.weapon.weaponType.humanName}"
          is MeleeDamageMessage -> "${it.attacker.type.humanName} hits ${it.target.type.humanName} with their ${it.attacker.weapon.weaponType.humanName} for ${it.damage} damage"
          is MeleeDefeatedMessage -> TODO()
        }
      }.forEach {
        logArea.addParagraph(it, withNewLine = false)
      }
    }
  }

  fun onUndock()
  {
    logUpdateSubscription.dispose()
  }

  fun add(messages: List<LogMessage>)
  {
    logMessages.addAll(messages)
  }
}
