package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.Component
import com.degrendel.outrogue.common.*

/** Indicates that this entity is currently visible to the Rogue. */
object VisibleComponent : Component

/** Indicates that this entity has been seen by the Rogue, even if it is not currently visible. */
object KnownComponent : Component

/** Tracks the position of this entity in the map. */
data class CoordinateComponent(val coordinate: Coordinate) : Component

/** Indicates this entity is a square in the level. */
data class SquareComponent(val square: Square) : Component

/** Tracks the id and dimensions of the room. */
data class RoomComponent(val room: Room) : Component

/** Indicates this entity is a creature. */
data class CreatureComponent(val creature: Creature) : Component

/** Indicates what team this creature belongs to. */
data class AllegianceComponent(val allegiance: Allegiance) : Component

