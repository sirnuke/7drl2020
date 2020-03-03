package com.degrendel.outrogue.common.world.creatures

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.agent.Controller
import com.degrendel.outrogue.common.world.Coordinate

interface Creature
{
  // Where this creature is located in the world (mutable)
  val coordinate: Coordinate
  // The Ashley entity for this creature
  val entity: Entity
  // The allegiance (usually mutable)
  val allegiance: Allegiance
  // What type of creature this is
  val type: CreatureType
  // The unique identifier for this creature
  val id: Int
  // When this creature can act again (mutable)
  val clock: Long
  // The AI controller for this creature (largely immutable)
  val controller: Controller
  // Whether this creature is active and can produce actions
  val active: Boolean
  // Whether this creature has been prodded to move out of the way
  val prodded: Boolean
}

enum class CreatureType(val humanName: String)
{
  ROGUE("Rogue"),
  CONJURER("Conjurer"),
  KESTREL("Kestrel")
  ;
}

enum class Allegiance
{
  ROGUE,
  CONJURER,
  NEUTRAL,
  ;

  fun isHostileTo(other: Allegiance) = when (this)
  {
    ROGUE -> other == CONJURER
    CONJURER -> other == ROGUE
    NEUTRAL -> false
  }
}

enum class ActiveStatus
{
  CONTACT,
  PRODDED,
  ASLEEP
}
