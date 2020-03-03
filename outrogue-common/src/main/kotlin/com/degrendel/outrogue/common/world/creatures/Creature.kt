package com.degrendel.outrogue.common.world.creatures

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.agent.Controller
import com.degrendel.outrogue.common.world.Coordinate

interface Creature
{
  val coordinate: Coordinate
  val entity: Entity
  val allegiance: Allegiance
  val type: CreatureType
  val id: Int
  val cooldown: Long
  val controller: Controller
  val active: Boolean
}

enum class CreatureType
{
  ROGUE,
  CONJURER,
  ;
}

enum class Allegiance
{
  ROGUE,
  CONJURER,
  NEUTRAL,
  ;
}
