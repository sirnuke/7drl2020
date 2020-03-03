package com.degrendel.outrogue.frontend.events

import com.degrendel.outrogue.common.world.EightWay
import org.hexworks.cobalt.events.api.Event

sealed class PlayerInputType
data class EightWayPress(val eightWay: EightWay) : PlayerInputType()
object UpstairsPress : PlayerInputType()
object DownstarsPress : PlayerInputType()
object SleepPress : PlayerInputType()

data class PlayerActionInput(val input: PlayerInputType, override val emitter: Any): Event