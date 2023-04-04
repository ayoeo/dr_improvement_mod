package com.beanz.drpings

import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigPanelHost
import net.minecraft.client.gui.GuiButton

class DRPingsPanel : AbstractConfigPanel() {
  override fun getPanelTitle() = "DR Pings Config"



  override fun onPanelHidden() {

    LiteLoader.getInstance().writeConfig(LiteModDRPings.mod)
  }

  override fun addOptions(host: ConfigPanelHost) {



  }
}
