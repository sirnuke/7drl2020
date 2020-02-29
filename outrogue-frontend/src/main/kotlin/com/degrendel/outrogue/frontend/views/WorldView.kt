package com.degrendel.outrogue.frontend.views

import com.degrendel.outrogue.common.PlayerInputProvider
import com.degrendel.outrogue.frontend.Application
import com.degrendel.outrogue.frontend.views.fragments.WorldFragment
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.view.base.BaseView

class InGameView(val app: Application) : PlayerInputProvider, BaseView(app.tileGrid)
{
  private val world = WorldFragment(app)

  init
  {
    screen.theme = ColorThemes.adriftInDreams()
  }
}