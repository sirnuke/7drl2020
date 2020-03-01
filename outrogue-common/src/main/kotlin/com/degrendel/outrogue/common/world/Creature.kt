package com.degrendel.outrogue.common.world

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.ai.Controller

interface Creature
{
  val coordinate: Coordinate
  val entity: Entity
  val allegiance: Allegiance
  val type: CreatureType
  val id: Int
  val cooldown: Long
  val controller: Controller
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
