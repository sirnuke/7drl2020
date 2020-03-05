package com.degrendel.outrogue.common.properties

import com.degrendel.outrogue.common.agent.Behavior
import com.degrendel.outrogue.common.world.creatures.Allegiance
import com.degrendel.outrogue.common.world.creatures.CreatureType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.random.Random

data class Properties(val window: Window, val map: Map, val views: Views, val costs: Costs, val conjurer: CoreCreature, val rogue: CoreCreature, val creatures: kotlin.collections.Map<CreatureType, CreatureDefinition>)
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

enum class Font
{
  CP47_16X16,
  CP47_12X12,
  CP47_10X10,
  ;
}

data class Window(val width: Int, val height: Int, val title: String, val fpsLimit: Int, val font: Font, val displayVersion: Boolean, val displayBuildDate: Boolean)
data class Map(val width: Int, val height: Int, val floors: Int, val rooms: Rooms, val features: Features)
data class Features(val maxPlacementAttempts: Int, // When to give up on attempting to randomly place something (might be an assert error, might skip)
                    val extraStaircases: List<Double>, // How many extra staircases to spawn?  Each one is a probability [0,1), lower is less likely
                    val monsterSpawnStart: Double, // How much 'cost' in monsters should be on the first level?
                    val monsterSpawnScale: Double // How much should this multiply per level?
)

data class ComponentDimensions(val x: Int, val y: Int, val width: Int, val height: Int)

data class Dice(val rolls: Int, val sides: Int)
{
  fun toInstance(random: Random) = com.degrendel.outrogue.common.world.Dice(random, rolls = rolls, sides = sides)
}

data class IntRange(val from: Int, val to: Int)
{
  fun toInstance() = (from..to)
}

data class Rooms(val minSize: Int, val maxNumber: Int)
data class World(val mapX: Int, val mapY: Int, val maxQueuedActions: Int, val sidebar: ComponentDimensions, val log: ComponentDimensions)
data class Costs(val sleep: Long, val move: Long, val staircase: Long, val prod: Long, val melee: Long)
data class Views(val world: World)
data class CoreCreature(val hp: Int, val strength: Int, val ac: Int, val toHit: Int, val melee: Dice)
data class CreatureDefinition(
    // Base HP of this creature
    val hp: Int,
    // Base strength of this creature
    val strength: Int,
    // The cost of this creature, when randomly spawning it
    val cost: Double,
    // The earliest level this creature can appear
    val earliestLevel: Int,
    // The default allegiance of this creature
    val allegiance: Allegiance,
    // Base AC for this creature
    val ac: Int,
    // Human readable name for their base armor
    val armorName: String,
    // Base toHit for their fists
    val toHit: Int,
    // Damage definition for their fists
    val damage: Dice,
    // The human readable name for their base unarmed weapon
    val weaponName: String,
    // List of behaviors, and their weights, to use in its AI
    val behaviors: kotlin.collections.Map<Behavior, Int>,
    // The navigation weight range to consider 'good' - probably should include 0
    val targetWeight: IntRange
)