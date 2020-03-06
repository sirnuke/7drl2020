package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.properties.Distribution
import com.degrendel.outrogue.common.world.DistributionType
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.AbstractRandomGenerator
import kotlin.random.Random

sealed class DistributionInstance
{
  abstract val type: DistributionType

  abstract fun sample(): Int
}

object ApacheCommonsHaveTerribleAPIsGenerator : AbstractRandomGenerator()
{
  lateinit var random: Random

  override fun setSeed(seed: Long) = throw IllegalAccessException("Don't you dare Apache")
  override fun nextDouble() = random.nextDouble()
}

class PoissonInstance(parameters: List<Double>) : DistributionInstance()
{
  override val type = DistributionType.POISSON

  private val source = PoissonDistribution(ApacheCommonsHaveTerribleAPIsGenerator, parameters[0], PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS)

  override fun sample() = source.sample()
}

fun Distribution.toInstance(): DistributionInstance
{
  return when (type)
  {
    DistributionType.POISSON -> PoissonInstance(parameters)
  }
}

