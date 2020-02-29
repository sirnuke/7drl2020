package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.WallOrientation
import org.hexworks.zircon.api.data.Tile
import java.util.*

object TileLibrary
{
  val floorTile = Tile.defaultTile().withCharacter('.')
  val blockedTile = Tile.defaultTile().withCharacter(' ')
  val corridorTile = Tile.defaultTile().withCharacter('#')
  val doorTile = Tile.defaultTile().withCharacter('+')
  val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())
  val conjurerTile = Tile.defaultTile().withCharacter('@')

  val wallTiles = EnumMap<WallOrientation, Tile>(WallOrientation::class.java)

  init
  {
    wallTiles[WallOrientation.NORTH_SOUTH] = Tile.defaultTile().withCharacter(0x2551.toChar())
    wallTiles[WallOrientation.EAST_WEST] = Tile.defaultTile().withCharacter(0x2550.toChar())
    wallTiles[WallOrientation.NORTH_EAST] = Tile.defaultTile().withCharacter(0x255A.toChar())
    wallTiles[WallOrientation.EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2554.toChar())
    wallTiles[WallOrientation.SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2557.toChar())
    wallTiles[WallOrientation.WEST_NORTH] = Tile.defaultTile().withCharacter(0x255D.toChar())
    wallTiles[WallOrientation.NORTH_EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2560.toChar())
    wallTiles[WallOrientation.EAST_SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2566.toChar())
    wallTiles[WallOrientation.SOUTH_WEST_NORTH] = Tile.defaultTile().withCharacter(0x2563.toChar())
    wallTiles[WallOrientation.WEST_NORTH_EAST] = Tile.defaultTile().withCharacter(0x2569.toChar())
    wallTiles[WallOrientation.ALL] = Tile.defaultTile().withCharacter(0x256C.toChar())
  }
}
