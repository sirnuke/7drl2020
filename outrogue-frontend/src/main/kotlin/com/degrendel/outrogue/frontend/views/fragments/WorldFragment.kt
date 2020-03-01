package com.degrendel.outrogue.frontend.views.fragments

import com.degrendel.outrogue.common.SquareType
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.frontend.Application
import com.degrendel.outrogue.frontend.TileLibrary
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

  var floor = 0

  fun refreshMap()
  {
    L.info("Refreshing map!")
    val level = app.engine.world.getLevel(floor)
    (0 until P.map.width).forEach { x ->
      (0 until P.map.height).forEach { y ->
        // TODO: Update visible, known variants
        val square = level.getSquare(x, y)
        L.trace("Setting ({},{}) of type {}", x, y, square.type)
        val tile = when (square.type)
        {
          SquareType.BLOCKED -> TileLibrary.blockedTile
          SquareType.CORRIDOR -> TileLibrary.corridorTile
          SquareType.FLOOR -> TileLibrary.floorTile
          SquareType.WALL -> TileLibrary.wallTiles.getValue(square.wallOrientation)
          SquareType.DOOR -> TileLibrary.doorTile
        }
        baseLayer[x, y] = tile
      }
    }
    baseLayer.draw()

    // TODO: visible/known effects for squares
    // TODO: fire/etc effects for squares?
    // TODO: draw creatures
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

    fun draw()
    {
      layer.draw(tiles)
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