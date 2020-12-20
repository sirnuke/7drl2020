package com.degrendel.outrogue.frontend

import com.badlogic.ashley.core.Entity
import com.degrendel.outrogue.common.agent.Action
import com.degrendel.outrogue.common.agent.Move
import com.degrendel.outrogue.common.agent.Sleep
import com.degrendel.outrogue.common.world.EightWay
import com.degrendel.outrogue.common.world.creatures.Creature

sealed class Intent
{
  abstract val complete: Boolean
}

sealed class PendingIntent : Intent()
{
  final override val complete = false

  abstract fun apply(creature: Creature, step: Step): Intent
}

sealed class CompletedIntent : Intent()
{
  final override val complete = true
}

object NextIntent : PendingIntent()
{
  override fun apply(creature: Creature, step: Step): Intent
  {
    return when (step)
    {
      is DirectionStep -> ValidIntent(Move(creature, step.eightWay))
      is SleepStep -> ValidIntent(Sleep(creature))
      // TODO: This is /probably/ a bug in the input handling
      is InventoryStep -> InvalidIntent("Not expecting an inventory slot!")
    }
  }
}

data class InvalidIntent(val message: String) : CompletedIntent()

data class ValidIntent(val action: Action) : CompletedIntent()

sealed class Step
object SleepStep : Step()
data class DirectionStep(val eightWay: EightWay) : Step()
data class InventoryStep(val slot: Int) : Step()