package com.degrendel.outrogue.frontend

import com.degrendel.outrogue.common.world.*
import org.hexworks.zircon.api.data.Tile
import java.util.*

object TileLibrary
{
  private val floorTile = Tile.defaultTile().withCharacter('.')
  private val blockedTile = Tile.defaultTile().withCharacter(' ')
  private val corridorTile = Tile.defaultTile().withCharacter('#')
  private val doorTile = Tile.defaultTile().withCharacter('+')
  private val downCaseTile = Tile.defaultTile().withCharacter('>')
  private val upCaseTile = Tile.defaultTile().withCharacter('<')
  private val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())
  private val conjurerTile = Tile.defaultTile().withCharacter('@')

  private val wallTiles = EnumMap<WallOrientation, Tile>(WallOrientation::class.java)

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

  fun getSquareTile(square: Square): Tile
  {
    return when (square.type)
    {
      SquareType.BLOCKED -> blockedTile
      SquareType.CORRIDOR -> corridorTile
      SquareType.FLOOR -> floorTile
      SquareType.WALL -> wallTiles.getValue(square.wallOrientation)
      SquareType.DOOR -> doorTile
      SquareType.STAIRCASE_DOWN -> downCaseTile
      SquareType.STAIRCASE_UP -> upCaseTile
    }
  }

  fun getCreatureTile(creature: Creature): Tile
  {
    return when (creature.type)
    {
      CreatureType.CONJURER -> conjurerTile
      CreatureType.ROGUE -> rogueTile
    }
  }
}
