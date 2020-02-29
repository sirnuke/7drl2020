package com.degrendel.outrogue.engine

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.Properties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class OutrogueEngine : Engine
{
  private val mapper = ObjectMapper().registerKotlinModule()

  override val properties: Properties = mapper.readValue(javaClass.getResource("/properties.json"))
}