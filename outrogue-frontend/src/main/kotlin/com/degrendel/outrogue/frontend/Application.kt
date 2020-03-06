package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.*
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.frontend.views.InGameView
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.Application
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

// TODO: Thinking inGame should implement frontend and be responsible for creating the engine
class Application(lock: ReentrantLock, condition: Condition, val profile: LaunchProfile)
{
  companion object
  {
    private val L by logger()
  }

  val tileGrid: TileGrid
  val inGameView: InGameView
  val application: Application

  init
  {

    val debugConfig = if (profile.debugDrawGrid)
      DebugConfig(displayGrid = true, displayCoordinates = true, displayFps = true)
    else
      DebugConfig(displayGrid = false, displayCoordinates = false, displayFps = true)

    var title = P.window.title

    if (P.window.displayVersion || P.window.displayBuildDate)
    {
      title += "  --  "
      if (P.window.displayVersion)
        title += "$VERSION_STRING "
      if (P.window.displayBuildDate)
        title += "built on $BUILD_DATE "
    }

    //val font = TrueTypeFontResources.ibmBios(16)
    val font = when(P.window.fontSize)
    {
      10 -> CP437TilesetResources.rexPaint10x10()
      12 -> CP437TilesetResources.rexPaint12x12()
      16 -> CP437TilesetResources.rexPaint16x16()
      else -> throw IllegalArgumentException("Unhandled font size ${P.window.fontSize}")
    }

    var appConfig = AppConfig.newBuilder()
        .withSize(P.window.width, P.window.height)
        .withDefaultTileset(font)
        .withDebugConfig(debugConfig)
        .withDebugMode(profile.zirconDebugMode)
        .withFpsLimit(P.window.fpsLimit)
        .withTitle(title)

    if (profile.fullscreen)
      appConfig = appConfig.fullScreen()

    application = SwingApplications.startApplication(appConfig.build())

    tileGrid = application.tileGrid

    tileGrid.onShutdown { lock.withLock { condition.signal() } }

    inGameView = InGameView(application, tileGrid, profile)

    if (profile.zirconDebugMode)
    {
      inGameView.screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
        if (event.code == KeyCode.ESCAPE)
          exitProcess(0)
        UIEventResponse.pass()
      }
    }

    tileGrid.dock(inGameView)
  }
}

data class LaunchProfile(val fullscreen: Boolean,
                         val rogueAgentDebugging: Boolean,
                         val rogueAgentLogging: Boolean,
                         val zirconDebugMode: Boolean,
                         val debugDrawGrid: Boolean,
                         val randomSeed: Long?)
