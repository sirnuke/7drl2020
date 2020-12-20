package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.*
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

@Command(name = "OUTROGUE", mixinStandardHelpOptions = true, version = [VERSION, GIT_SHA, DIRTY.toString(), BUILD_DATE])
class Main : Callable<Int>
{
  companion object
  {
    private val L by logger()
  }

  private val lock = ReentrantLock()
  private val condition = lock.newCondition()

  @Option(names = ["--rogue-agent-debugging"])
  private var rogueAgentDebugging = false

  @Option(names = ["--rogue-agent-logging"])
  private var rogueAgentLogging = false

  @Option(names = ["--zircon-debug-mode"])
  private var zirconDebugMode = false

  @Option(names = ["--draw-zircon-grid"])
  private var drawZirconGrid = false

  @Option(names = ["--fullscreen"])
  private var fullscreen = false

  @Option(names = ["--random-seed"], description = ["Override the random seed"])
  private var randomSeed: Long? = null

  override fun call(): Int
  {
    L.info("Outrogue {}", VERSION_STRING)
    L.info("Built on {}", BUILD_DATE)
    val launchProfile = LaunchProfile(fullscreen = fullscreen, rogueAgentDebugging = rogueAgentDebugging,
        rogueAgentLogging = rogueAgentLogging, debugDrawGrid = drawZirconGrid, zirconDebugMode = zirconDebugMode,
        randomSeed = randomSeed)
    L.info("Launching with {}", launchProfile)
    Application(lock, condition, launchProfile)
    lock.withLock { condition.await() }
    L.info("Quiting...")
    return 0
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
