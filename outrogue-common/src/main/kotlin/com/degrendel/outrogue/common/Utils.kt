package com.degrendel.outrogue.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <R : Any> R.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this::class.java) }

val VERSION_STRING = "$VERSION $GIT_SHA${if (DIRTY == 1) "-dirty" else ""}"

