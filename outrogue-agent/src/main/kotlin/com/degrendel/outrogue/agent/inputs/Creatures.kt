package com.degrendel.outrogue.agent.inputs

import com.degrendel.outrogue.common.world.Coordinate
import com.degrendel.outrogue.common.world.World
import com.degrendel.outrogue.common.world.creatures.Allegiance
import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.creatures.CreatureType

data class CreatureInput(val id: Int,
                         val type: CreatureType,
                         val position: Coordinate,
                         val visible: Boolean,
                         val friendly: Boolean,
                         val hostile: Boolean)

fun Creature.toInput(world: World): CreatureInput
{
  val visible = world.getSquare(coordinate).visibleToRogue
  return CreatureInput(id = id, type = type, position = coordinate, visible = visible,
      friendly = allegiance == Allegiance.ROGUE, hostile = allegiance == Allegiance.CONJURER)
}

