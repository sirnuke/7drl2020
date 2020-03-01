package com.degrendel.outrogue.common

import com.badlogic.ashley.core.Entity

interface Creature
{
  val coordinate: Coordinate
  val entity: Entity
  val allegiance: Allegiance
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
