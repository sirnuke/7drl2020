package com.degrendel.outrogue.engine

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.world.*
import com.degrendel.outrogue.common.world.creatures.Creature
import kotlin.math.abs

class SquareState(override val coordinate: Coordinate, var _type: SquareType, override val room: Int?, var _creature: CreatureState? = null, var _staircase: Int? = null) : Square
{
  var _visible = mutableSetOf<Int>().also { if (room != null) it.add(room) }
  override val visible: Set<Int> get() = _visible
  override val entity: Entity = Entity()
      .add(CoordinateComponent(coordinate))
      .add(SquareComponent(this))

  override val type: SquareType get() = _type
  override val staircase get() = _staircase

  override val creature: Creature? get() = _creature


  private var _visibleToRogue = false
  override val visibleToRogue get() = _visibleToRogue
  private var _knownToRogue = false
  override val knownToRogue get() = _knownToRogue

  var _wallOrientation = WallOrientation.NONE

  override val wallOrientation get() = _wallOrientation

  fun setOnVisibleLevel(visible: Boolean)
  {
    if (visible)
      entity.add(OnVisibleLevelComponent)
    else
      entity.remove(OnVisibleLevelComponent::class.java)
  }

  fun setRogueVisible(visible: Boolean)
  {
    _visibleToRogue = visible
    if (visible)
    {
      _knownToRogue = true
      entity.add(KnownToRogueComponent)
      entity.add(VisibleToRogueComponent)
    }
    else
      entity.remove(VisibleToRogueComponent::class.java)
  }
}