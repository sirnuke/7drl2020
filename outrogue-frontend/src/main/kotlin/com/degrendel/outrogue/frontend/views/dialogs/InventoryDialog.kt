package com.degrendel.outrogue.frontend.views.dialogs

import com.degrendel.outrogue.common.Engine
import com.degrendel.outrogue.common.properties.Properties.Companion.P
import org.hexworks.zircon.api.ComponentDecorations
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.screen.Screen

class InventoryDialog(private val engine: Engine, private val screen: Screen)
{
  private val panel = Components.panel()
      .withDecorations(ComponentDecorations.box(title = "YES?"))
      .withPosition(0, 0)
      .withSize(P.window.width, P.window.height)
      .build()

  init
  {
    screen.addComponent(panel)
    panel.isHidden = true
  }

  fun hide()
  {
    panel.isHidden = true
  }

  fun show()
  {
    panel.isHidden = false
  }

  fun refresh()
  {
    // TODO: Stub!
  }
}