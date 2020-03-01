package com.degrendel.outrogue.frontend.views.fragments

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.degrendel.outrogue.common.components.*
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.frontend.Application
import com.degrendel.outrogue.frontend.TileLibrary
import com.degrendel.outrogue.frontend.components.DrawnAtComponent
import com.degrendel.outrogue.frontend.components.getDrawnAt
import com.degrendel.outrogue.frontend.components.isEqual
import com.degrendel.outrogue.frontend.components.toPosition
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.Layer
import org.hexworks.zircon.api.screen.Screen

/**
 * Handles drawing the map.
 *
 * Not technically a fragment since it's a series of layers, but whatever.
 */
class WorldFragment(val app: Application, screen: Screen)
{
  companion object
  {
    private val L by logger()
  }

  private val baseLayer = LayerData().also { screen.addLayer(it.layer) }
  private val creatureLayer = LayerData().also { screen.addLayer(it.layer) }

  private val visibleSquares = Family.all(SquareComponent::class.java, OnVisibleLevel::class.java).get()

  private val toDrawCreatures = Family.all(CreatureComponent::class.java, OnVisibleLevel::class.java).exclude(DrawnAtComponent::class.java).get()
  private val toEraseCreatures = Family.all(CreatureComponent::class.java, DrawnAtComponent::class.java).exclude(OnVisibleLevel::class.java).get()
  private val toUpdateCreatures = Family.all(CreatureComponent::class.java, OnVisibleLevel::class.java, DrawnAtComponent::class.java).get()

  init
  {
    app.engine.ecs.addEntityListener(visibleSquares, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        val square = entity.getSquare()
        val position = entity.getCoordinate().toPosition()
        L.trace("Setting ({},{}) of type {}", position.x, position.y, square.type)
        baseLayer[position] = TileLibrary.getSquareTile(square)
      }

      override fun entityRemoved(entity: Entity)
      {
      }
    })
    app.engine.ecs.addEntityListener(toDrawCreatures, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.debug("Drawing creature {}", entity.getCreature())
        val position = entity.getCoordinate().toPosition()
        entity.add(DrawnAtComponent(position))
        creatureLayer[position] = TileLibrary.getCreatureTile(entity.getCreature())
      }

      override fun entityRemoved(entity: Entity)
      {
      }
    })

    app.engine.ecs.addEntityListener(toEraseCreatures, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.debug("Erasing creature {}", entity.getCreature())
        val position = entity.getDrawnAt()
        entity.remove(DrawnAtComponent::class.java)
        creatureLayer.erase(position)
      }

      override fun entityRemoved(entity: Entity)
      {
      }
    })
  }

  fun refreshMap()
  {
    // NOTE: to add 'blocking' animations, start them as suspendCoroutines that terminate on the callback.  Return the
    // list of coroutines.  Can't really do that for moving, not at least without doing it through GDC
    L.trace("Refreshing map")
    baseLayer.draw()

    val updatedCreatures = mutableSetOf<Position>()

    // TODO: Might be faster to copy/erase the entities from a set using a listener
    app.engine.ecs.getEntitiesFor(toUpdateCreatures).forEach {
      L.trace("Updating drawn creature {}", it.getCreature())
      val position = it.getDrawnAt()
      val coordinate = it.getCoordinate()
      // Shortcut: drawn == coordinate? Nothing to do
      if (coordinate.isEqual(position)) return@forEach
      val newPosition = coordinate.toPosition()
      // if the old position hasn't been updated this cycle, erase it
      if (position !in updatedCreatures)
        creatureLayer.erase(position)
      creatureLayer[newPosition] = TileLibrary.getCreatureTile(it.getCreature())
      updatedCreatures.add(newPosition)
      it.add(DrawnAtComponent(newPosition))
    }
    creatureLayer.draw()
    // TODO: visible/known effects for squares
    // TODO: fire/etc effects for squares?
  }

  class LayerData
  {
    val layer: Layer = Layer.newBuilder().withOffset(offset).withSize(size).build()
    private val _tiles = tileSource.toMutableMap()

    val tiles: Map<Position, Tile> get() = _tiles

    operator fun set(x: Int, y: Int, tile: Tile)
    {
      _tiles[Position.create(x, y)] = tile
    }

    operator fun set(position: Position, tile: Tile)
    {
      _tiles[position] = tile
    }

    operator fun get(position: Position) = _tiles[position]!!

    fun draw()
    {
      layer.draw(tiles)
    }

    fun erase(position: Position)
    {
      _tiles[position] = Tile.empty()
    }

    companion object
    {
      private val offset = Position.create(P.views.world.mapX, P.views.world.mapY)
      private val size = Size.create(P.map.width, P.map.height)
      private val tileSource = mutableMapOf<Position, Tile>()

      // TODO: This is a bit clunk, though probably fine (only create a few layers after all)
      init
      {
        (0 until size.width).forEach { x ->
          (0 until size.height).forEach { y ->
            tileSource[Position.create(x, y)] = Tile.empty()
          }
        }
      }
    }
  }
}