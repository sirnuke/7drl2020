package com.degrendel.outrogue.frontend.components

import com.badlogic.ashley.core.Component
import com.degrendel.outrogue.common.Coordinate
import org.hexworks.zircon.api.data.Position

/** Stores where this entity is currently drawn. */
data class DrawnAtComponent(val position: Position) : Component

fun Coordinate.toPosition() = Position.create(this.x, this.y)

fun Coordinate.isEqual(position: Position) = (this.x == position.x && this.y == position.y)
