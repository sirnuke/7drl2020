package com.degrendel.outrogue.common.world

import kotlin.random.Random

data class Dice(val rolls: Int, val sides: Int, val random: Random)
{
  fun roll(): Int
  {
    return (0 until rolls).reduce { acc, _ -> random.nextInt(1, sides + 1) }
  }
}