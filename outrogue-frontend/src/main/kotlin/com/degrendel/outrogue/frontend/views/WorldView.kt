package com.degrendel.outrogue.frontend.views

import com.degrendel.outrogue.common.Frontend
import com.degrendel.outrogue.common.logger
import com.degrendel.outrogue.frontend.Application
import com.degrendel.outrogue.frontend.views.fragments.WorldFragment
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.view.base.BaseView

class InGameView(val app: Application) : BaseView(app.tileGrid)
{
  companion object
  {
    private val L by logger()
  }

  private val world = WorldFragment(app, screen)

  init
  {
    screen.theme = ColorThemes.adriftInDreams()
  }

  fun refreshMap(floor: Int)
  {
    world.floor = floor
    world.refreshMap()
  }

  override fun onDock()
  {
    L.info("Docking InGameView")
    world.refreshMap()
  }

  override fun onUndock()
  {
    // TODO: Do we need to do anything?  Probably stop producing player input for starters
  }
}