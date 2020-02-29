package com.degrendel.outrogue.common

data class Properties(val window: Window)
data class Window(val width: Int, val height: Int, val title: String, val fpsLimit: Int)