package com.degrendel.outrogue.frontend.views

import com.degrendel.outrogue.common.agent.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.frontend.Application
import com.degrendel.outrogue.frontend.events.*
import com.degrendel.outrogue.frontend.views.fragments.WorldFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import org.hexworks.cobalt.events.api.simpleSubscribeTo
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.api.view.base.BaseView
import org.hexworks.zircon.internal.Zircon

class InGameView(val app: Application) : BaseView(app.tileGrid)
{
  companion object
  {
    private val L by logger()
  }

  private val world = WorldFragment(app, screen)
  private var job: Job? = null

  // While this initial channel is not listened to (new one is created before the game loop starts), create it anyway
  // so any inputs that happen to come in before onDock completes don't trigger a lateinit error
  private var playerActions = Channel<PlayerInputType>(capacity = P.views.world.maxQueuedActions)

  init
  {
    screen.theme = ColorThemes.adriftInDreams()
    // TODO: These won't fire if something else is docked, right?
    screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
      // TODO: Would be nice to have this be configurable
      when (event.code)
      {
        KeyCode.LEFT, KeyCode.KEY_A, KeyCode.NUMPAD_4 ->
        {
          L.trace("Move WEST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.WEST), this))
          Processed
        }
        KeyCode.RIGHT, KeyCode.KEY_D, KeyCode.NUMPAD_6 ->
        {
          L.trace("Move EAST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.EAST), this))
          Processed
        }
        KeyCode.UP, KeyCode.KEY_W, KeyCode.NUMPAD_8 ->
        {
          L.trace("Move NORTH pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.NORTH), this))
          Processed
        }
        KeyCode.DOWN, KeyCode.KEY_X, KeyCode.NUMPAD_2 ->
        {
          L.trace("Move SOUTH pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.SOUTH), this))
          Processed
        }
        KeyCode.HOME, KeyCode.KEY_Q, KeyCode.NUMPAD_7 ->
        {
          L.trace("Move NORTH_WEST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.NORTH_WEST), this))
          Processed
        }
        KeyCode.PAGE_UP, KeyCode.KEY_E, KeyCode.NUMPAD_9 ->
        {
          L.trace("Move NORTH_EAST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.NORTH_EAST), this))
          Processed
        }
        KeyCode.END, KeyCode.KEY_Z, KeyCode.NUMPAD_1 ->
        {
          L.trace("Move SOUTH_WEST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.SOUTH_WEST), this))
          Processed
        }
        KeyCode.PAGE_DOWN, KeyCode.KEY_C, KeyCode.NUMPAD_3 ->
        {
          L.trace("Move SOUTH_EAST pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(EightWayPress(EightWay.SOUTH_EAST), this))
          Processed
        }
        KeyCode.KEY_S, KeyCode.NUMPAD_5 ->
        {
          L.trace("Sleep pressed {}", event.code)
          Zircon.eventBus.publish(PlayerActionInput(SleepPress, this))
          Processed
        }
        KeyCode.COMMA ->
        {
          if (event.shiftDown)
          {
            L.trace("Go Up pressed {}", event.code)
            Zircon.eventBus.publish(PlayerActionInput(UpstairsPress, this))
            Processed
          }
          else
            Pass
        }
        KeyCode.PERIOD ->
        {
          if (event.shiftDown)
          {
            L.trace("Go Down pressed {}", event.code)
            Zircon.eventBus.publish(PlayerActionInput(DownstarsPress, this))
            Processed
          }
          else
            Pass
        }
        else ->
        {
          L.trace("Ignoring {}", event.code)
          Pass
        }
      }
    }

    Zircon.eventBus.simpleSubscribeTo<PlayerActionInput> {
      L.trace("Event player input {}", it.input)
      playerActions.offer(it.input)
    }
  }

  fun refreshMap()
  {
    world.refreshMap()
  }

  override fun onDock()
  {
    L.info("Docking InGameView")
    world.refreshMap()
    // Recreate the channel, since closing it
    playerActions = Channel(capacity = P.views.world.maxQueuedActions)
    job = app.engine.runGame()
  }

  // TODO: Not clear what should be calling this, but this is how stopping the loop should work
  suspend fun stopGameLoop()
  {
    // TODO: Does the channel need to be explicitly closed?
    job?.cancelAndJoin()
    job = null
  }

  suspend fun getPlayerInput(): Action
  {
    // TODO: Do we have to check whether we've been cancelled? I think .receive() should do it for us
    // TODO: We probably need to catch the closed exception, but where and what do we do next?
    val player = app.engine.world.conjurer
    var action: Action?
    do
    {
      action = when (val input = playerActions.receive())
      {
        is UpstairsPress -> GoUpStaircase(player)
        is DownstarsPress -> GoDownStaircase(player)
        is EightWayPress -> app.engine.contextualAction(input.eightWay)
        is SleepPress -> Sleep(player)
      }
    }
    while (action == null || !app.engine.isValidAction(action))
    return action
  }

  override fun onUndock()
  {
    // TODO: Do we need to do anything here?
  }
}