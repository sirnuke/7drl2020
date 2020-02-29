package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.engine.OutrogueEngine
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

class Application(lock: ReentrantLock, condition: Condition, profile: LaunchProfile)
{
  companion object
  {
    val L by logger()
  }

  val engine = OutrogueEngine()

  val tileGrid: TileGrid

  init
  {
    if (profile.soarDebugger)
      engine.openAgentDebuggers()

    val debugConfig = if (profile.debugDrawGrid)
      DebugConfig(displayGrid = true, displayCoordinates = true, displayFps = true)
    else
      DebugConfig(displayGrid = false, displayCoordinates = false, displayFps = true)

    tileGrid = SwingApplications.startTileGrid(
        AppConfig(
            blinkLengthInMilliSeconds = 500,
            cursorStyle = CursorStyle.FIXED_BACKGROUND,
            cursorColor = TileColor.defaultForegroundColor(),
            isCursorBlinking = false,
            isClipboardAvailable = true,
            defaultTileset = CP437TilesetResources.rexPaint16x16(),
            defaultGraphicalTileset = CP437TilesetResources.rexPaint16x16(),
            defaultColorTheme = ColorThemes.defaultTheme(),
            title = engine.properties.window.title,
            fullScreen = profile.fullscreen,
            debugMode = profile.zirconDebugMode,
            debugConfig = debugConfig,
            size = Size.create(engine.properties.window.width, engine.properties.window.height),
            betaEnabled = false,
            fpsLimit = engine.properties.window.fpsLimit))

    tileGrid.onShutdown { lock.withLock { condition.signal() } }
  }
}

data class LaunchProfile(val fullscreen: Boolean,
                         val soarDebugger: Boolean,
                         val zirconDebugMode: Boolean,
                         val debugDrawGrid: Boolean)
