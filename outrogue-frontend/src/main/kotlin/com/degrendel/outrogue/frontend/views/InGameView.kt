package com.degrendel.outrogue.frontend.views

import com.degrendel.outrogue.common.PlayerInputProvider
import com.degrendel.outrogue.frontend.Application
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.view.base.BaseView

class InGameView(val app: Application) : PlayerInputProvider, BaseView(app.tileGrid)
{
  init
  {
    screen.theme = ColorThemes.adriftInDreams()
  }
}