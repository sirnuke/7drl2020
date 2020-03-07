package com.degrendel.outrogue.frontend.views.fragments

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import org.hexworks.zircon.api.ComponentDecorations
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.screen.Screen

class ConjurerFragment(private val engine: Engine, private val screen: Screen)
{
  private val panel = Components.panel()
      .withSize(P.views.world.conjurer.width, P.views.world.conjurer.height)
      .withPosition(P.views.world.conjurer.x, P.views.world.conjurer.y)
      .withDecorations(ComponentDecorations.box(title = "You"))
      .build()

  init
  {
    screen.addComponent(panel)
  }
}