package com.degrendel.outrogue.frontend.views

import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.agent.*
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.engine.EngineState
import com.degrendel.outrogue.frontend.LaunchProfile
import com.degrendel.outrogue.frontend.events.*
import com.degrendel.outrogue.frontend.views.dialogs.InventoryDialog
import com.degrendel.outrogue.frontend.views.fragments.ConjurerFragment
import com.degrendel.outrogue.frontend.views.fragments.LogFragment
import com.degrendel.outrogue.frontend.views.fragments.RogueFragment
import com.degrendel.outrogue.frontend.views.fragments.WorldFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import org.hexworks.cobalt.events.api.*
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.ComponentDecorations.box
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.TrueTypeFontResources
import org.hexworks.zircon.api.application.Application
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.api.view.base.BaseView
import org.hexworks.zircon.internal.Zircon
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InGameView(private val application: Application, private val tileGrid: TileGrid, val profile: LaunchProfile) : BaseView(tileGrid), Frontend
{
  companion object
  {
    private val L by logger()
  }

  private val engine = EngineState(this, profile.randomSeed)

  private val world = WorldFragment(engine, profile, screen)
  private val logArea = LogFragment(application, screen)
  private val conjurerPanel = ConjurerFragment(engine, screen)
  private val roguePanel = RogueFragment(engine, screen)

  private val inventoryDialog = InventoryDialog(engine, screen)

  private var job: Job? = null

  init
  {
    screen
    if (profile.rogueAgentDebugging)
      engine.rogueAgent.enableDebugging()

    if (profile.rogueAgentLogging)
      engine.rogueAgent.enableLogging()

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
        KeyCode.BACK_QUOTE ->
        {
          if (profile.zirconDebugMode)
          {
            world.toggleDrawingDebugMap()
            Processed
          }
          else
            Pass
        }
        KeyCode.KEY_I ->
        {
          world.hide()
          inventoryDialog.show()
          Processed
        }
        else ->
        {
          L.trace("Ignoring {}", event.code)
          Pass
        }
      }
    }

    engine.bootstrapECS()
  }

  override suspend fun refresh()
  {
    world.refresh()
    conjurerPanel.refresh()
    roguePanel.refresh()
  }

  override fun onDock()
  {
    L.info("Docking InGameView")

    world.refresh()
    conjurerPanel.refresh()
    roguePanel.refresh()
    job = engine.runGame()
  }

  override fun onUndock()
  {
    logArea.onUndock()
    // TODO: Do we need to do anything here? Shutdown the engine? Dispose drools?
  }

  // TODO: Not clear what should be calling this, but this is how stopping the loop should work
  suspend fun stopGameLoop()
  {
    // TODO: Does the channel need to be explicitly closed?
    job?.cancelAndJoin()
    job = null
  }

  override suspend fun getPlayerInput(): Action = suspendCoroutine { continuation ->
    val player = engine.world.conjurer
    Zircon.eventBus.subscribeTo<PlayerActionInput> {
      val action = when (val input = it.input)
      {
        is UpstairsPress -> GoUpStaircase(player)
        is DownstarsPress -> GoDownStaircase(player)
        is EightWayPress -> engine.contextualAction(player, input.eightWay)
        is SleepPress -> Sleep(player)
      }
      if (action == null || !engine.isValidAction(action))
        KeepSubscription
      else
      {
        continuation.resume(action)
        DisposeSubscription
      }
    }
  }

  override fun drawNavigationMap(map: NavigationMap) = world.setDebugNavigationMap(map)

  override fun drawDebug(x: Int, y: Int, value: Int) = world.setDebugTile(x, y, value)

  override fun addLogMessages(messages: List<LogMessage>) = logArea.add(messages)

  enum class ViewState
  {
    MAP,
    INVENTORY,
    ;
  }
}