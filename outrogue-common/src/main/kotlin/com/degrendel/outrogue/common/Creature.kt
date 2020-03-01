package com.degrendel.outrogue.common

import com.badlogic.ashley.core.Entity

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
