package com.degrendel.outrogue.common.properties

import com.degrendel.outrogue.common.agent.Behavior
import com.degrendel.outrogue.common.world.creatures.Allegiance
import com.degrendel.outrogue.common.world.creatures.CreatureType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

data class Properties(val window: Window, val map: Map, val views: Views, val costs: Costs, val creatures: kotlin.collections.Map<CreatureType, Creature>)
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

data class Window(val width: Int, val height: Int, val title: String, val fpsLimit: Int, val displayVersion: Boolean, val displayBuildDate: Boolean)
data class Map(val width: Int, val height: Int, val floors: Int, val rooms: Rooms, val features: Features)
data class Features(val maxPlacementAttempts: Int, // When to give up on attempting to randomly place something (might be an assert error, might skip)
                    val extraStaircases: List<Double>, // How many extra staircases to spawn?  Each one is a probability [0,1), lower is less likely
                    val monsterSpawnStart: Double, // How much 'cost' in monsters should be on the first level?
                    val monsterSpawnScale: Double // How much should this multiply per level?
)

data class Rooms(val minSize: Int, val maxNumber: Int)
data class Views(val world: World)
data class World(val mapX: Int, val mapY: Int, val maxQueuedActions: Int)
data class Costs(val sleep: Long, val move: Long, val staircase: Long)
data class Creature(val hp: Int, // The base HP of this creature
                    val cost: Double, // The cost of this creature, when randomly spawning it
                    val earliestLevel: Int, // The earliest level this creature can appear
                    val allegiance: Allegiance, // The default allegiance of this creature
                    val behaviors: List<Behavior> // List of behaviors to use in its AI
)