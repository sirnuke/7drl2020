package com.degrendel.outrogue.agent.inputs

import com.degrendel.outrogue.common.world.EightWay

data class ExploreOption(val direction: EightWay, val cost: Int) : AutoClean