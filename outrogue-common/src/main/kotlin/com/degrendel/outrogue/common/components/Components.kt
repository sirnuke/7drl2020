package com.degrendel.outrogue.common.components

import com.badlogic.ashley.core.Component
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.creatures.Creature
import com.degrendel.outrogue.common.world.items.Item

/** Indicates that this entity is currently visible to the Rogue. */
object VisibleToRogueComponent : Component

/** Indicates that this entity has been seen by the Rogue, even if it is not currently visible. */
object KnownToRogueComponent : Component

/** Indicates that this entity is on the current visible level, from the Conjurer's point of view. */
object OnVisibleLevelComponent : Component

/** Indicates that this creature is active, and its controller should fire. */
object ActiveComponent : Component

/** Tracks the position of this entity in the map. */
data class CoordinateComponent(val coordinate: Coordinate) : Component

/** Indicates this entity is a square in the level. */
data class SquareComponent(val square: Square) : Component

/** Tracks the id and dimensions of the room. */
data class RoomComponent(val room: Room) : Component

/** Indicates this entity is a creature. */
data class CreatureComponent(val creature: Creature) : Component

/** Indicates this entity is an item. */
data class ItemComponent(val item: Item): Component

/** Indicates this creature belongs to the Rogue team. */
object RogueAllegianceComponent : Component

/** Indicates this creature belongs to the Conjurer team. */
object ConjurerAllegianceComponent : Component

/** Indicate this creature belongs to the Neutral team. */
object NeutralAllegianceComponent : Component
