package com.degrendel.outrogue.frontend.views.fragments

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import org.hexworks.zircon.api.ComponentDecorations
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.screen.Screen

class RogueFragment(private val engine: Engine, private val screen: Screen)
{
  private val panel = Components.panel()
      .withSize(P.views.world.rogue.width, P.views.world.rogue.height)
      .withPosition(P.views.world.rogue.x, P.views.world.rogue.y)
      .withDecorations(ComponentDecorations.box(title = "Enemy"))
      .build()

  init
  {
    screen.addComponent(panel)
  }

  fun refresh()
  {
    // TODO: Stub!
  }
}