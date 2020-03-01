package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.Frontend
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.engine.OutrogueEngine
import com.degrendel.outrogue.frontend.views.InGameView
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.CursorStyle
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.grid.TileGrid
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    var appConfig = AppConfig.newBuilder()
        .withSize(P.window.width, P.window.height)
        .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
        .withDebugConfig(debugConfig)
        .withDebugMode(profile.zirconDebugMode)
        .withFpsLimit(P.window.fpsLimit)
        .withTitle(P.window.title)

    if (profile.fullscreen)
      appConfig = appConfig.fullScreen()

    tileGrid = SwingApplications.startTileGrid(appConfig.build())

    tileGrid.onShutdown { lock.withLock { condition.signal() } }

    inGameView = InGameView(this)

    tileGrid.dock(inGameView)
  }

  override fun refreshMap(floor: Int)
  {
  }

}

data class LaunchProfile(val fullscreen: Boolean,
                         val soarDebugger: Boolean,
                         val zirconDebugMode: Boolean,
                         val debugDrawGrid: Boolean)
