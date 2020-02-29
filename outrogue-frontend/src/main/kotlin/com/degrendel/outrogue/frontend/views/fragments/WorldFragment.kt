package com.degrendel.outrogue.frontend.views.fragments

import com.degrendel.outrogue.common.properties.Properties.Companion.P
import com.degrendel.outrogue.frontend.Application
import kotlinx.collections.immutable.toImmutableMap
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.Layer

/**
 * Handles drawing the map.
 *
 * Not technically a fragment since it's a series of layers, but whatever.
 */
class WorldFragment(val app: Application)
{
  private val baseLayer = LayerData()
  private val creatureLayer = LayerData()

  data class LayerData(
      val layer: Layer = Layer.newBuilder().withOffset(offset).withSize(size).build(),
      val tiles: Map<Position, Tile> = tileSource.toImmutableMap()
  )
  {
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