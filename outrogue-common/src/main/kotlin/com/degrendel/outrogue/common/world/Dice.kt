package com.degrendel.outrogue.common.world

import kotlin.random.Random

data class Dice(val random: Random, val rolls: Int, val sides: Int)
{
  fun roll(): Int
  {
    return (0 until rolls).reduce { acc, _ -> acc + random.nextInt(1, sides + 1) }
  }
}