package com.degrendel.outrogue.agent.inputs

import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.Square
import com.degrendel.outrogue.common.world.creatures.Creature

data class Neighbor(val direction: EightWay, val square: Square, val creature: Creature?): AutoClean