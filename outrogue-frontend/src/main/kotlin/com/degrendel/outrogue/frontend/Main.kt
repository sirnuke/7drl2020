package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.logger
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

@Command(name = "Outrogue", mixinStandardHelpOptions = true)
class Main : Callable<Int>
{
  companion object
  {
    private val L by logger()
  }

  private val lock = ReentrantLock()
  private val condition = lock.newCondition()

  @Option(names = ["--soar-debugger"])
  private var soarDebugger = false

  @Option(names = ["--zircon-debug-mode"])
  private var zirconDebugMode = false

  @Option(names = ["--draw-zircon-grid"])
  private var drawZirconGrid = false

  @Option(names = ["--fullscreen"])
  private var fullscreen = false

  override fun call(): Int
  {
    val launchProfile = LaunchProfile(fullscreen = fullscreen, soarDebugger = soarDebugger,
        debugDrawGrid = drawZirconGrid, zirconDebugMode = zirconDebugMode)
    L.info("Launching with profile {}", launchProfile)
    Application(lock, condition, launchProfile)
    lock.withLock { condition.await() }
    L.info("Quiting...")
    return 0
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine.call(Main(), *args))
