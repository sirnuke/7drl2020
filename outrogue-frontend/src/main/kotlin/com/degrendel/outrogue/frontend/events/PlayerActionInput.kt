package com.degrendel.outrogue.frontend.events

import com.degrendel.outrogue.common.ai.Action
import org.hexworks.cobalt.events.api.Event

data class PlayerActionInput(val action: Action, override val emitter: Any): Event