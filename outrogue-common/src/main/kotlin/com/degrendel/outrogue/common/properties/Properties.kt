package com.degrendel.outrogue.common.properties

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

data class Properties(val window: Window, val map: Map, val views: Views)
{
  companion object
  {
    val P: Properties

    init
    {
      val mapper = ObjectMapper().registerKotlinModule().also { it.propertyNamingStrategy = PropertyNamingStrategy.KEBAB_CASE }

      P = mapper.readValue(Properties::class.java.getResource("/properties.json"))
    }
  }
}

data class Window(val width: Int, val height: Int, val title: String, val fpsLimit: Int)
data class Map(val width: Int, val height: Int, val floors: Int)
data class Views(val world: World)
data class World(val mapX: Int, val mapY: Int)