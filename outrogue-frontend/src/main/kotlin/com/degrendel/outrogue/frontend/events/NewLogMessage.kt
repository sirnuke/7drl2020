package com.degrendel.outrogue.frontend.events

import org.hexworks.cobalt.events.api.Event

data class NewLogMessage(val message: String, override val emitter: Any): Event
