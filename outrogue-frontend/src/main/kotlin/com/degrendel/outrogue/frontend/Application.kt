package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.BUILD_DATE
import com.degrendel.outrogue.common.Frontend
import com.degrendel.outrogue.common.VERSION_STRING
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.engine.OutrogueEngine
import com.degrendel.outrogue.frontend.views.InGameView
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

class Application(lock: ReentrantLock, condition: Condition, profile: LaunchProfile) : Frontend
{
  companion object
  {
    val L by logger()
  }

  val tileGrid: TileGrid
  val inGameView: InGameView
  val engine = OutrogueEngine(this)

  init
  {
    if (profile.soarDebugger)
      engine.openAgentDebuggers()

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

    var appConfig = AppConfig.newBuilder()
        .withSize(P.window.width, P.window.height)
        .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
        .withDebugConfig(debugConfig)
        .withDebugMode(profile.zirconDebugMode)
        .withFpsLimit(P.window.fpsLimit)
        .withTitle(title)

    if (profile.fullscreen)
      appConfig = appConfig.fullScreen()

    tileGrid = SwingApplications.startTileGrid(appConfig.build())

    tileGrid.onShutdown { lock.withLock { condition.signal() } }

    inGameView = InGameView(this)

    if (profile.zirconDebugMode)
    {
      inGameView.screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
        if (event.code == KeyCode.ESCAPE)
          exitProcess(0)
        UIEventResponse.pass()
      }
    }

    tileGrid.dock(inGameView)

    engine.bootstrapECS()
    refreshMap(0)

    // TODO: Start the main loop?
  }

  override fun refreshMap(floor: Int)
  {
    inGameView.refreshMap(floor)
  }
}

data class LaunchProfile(val fullscreen: Boolean,
                         val soarDebugger: Boolean,
                         val zirconDebugMode: Boolean,
                         val debugDrawGrid: Boolean)
