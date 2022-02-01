package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigPanelHost
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiSlider

class DRImprovementConfigPanel : AbstractConfigPanel() {
  override fun getPanelTitle() = "DR Improvement Config"

  private lateinit var energyWidth: ConfigTextField
  private lateinit var energyOffset: ConfigTextField

  private lateinit var textXOffset: ConfigTextField
  private lateinit var textYOffset: ConfigTextField

  override fun onPanelHidden() {
    this.energyWidth.text.toFloatOrNull()?.let {
      LiteModDRImprovement.mod.energyBarWidth = it
    }
    this.energyOffset.text.toFloatOrNull()?.let {
      LiteModDRImprovement.mod.energyBarOffset = it
    }
    this.textXOffset.text.toIntOrNull()?.let {
      LiteModDRImprovement.mod.textXOffset = it
    }
    this.textYOffset.text.toIntOrNull()?.let {
      LiteModDRImprovement.mod.textYOffset = it
    }

    LiteLoader.getInstance().writeConfig(LiteModDRImprovement.mod)
  }

  override fun addOptions(host: ConfigPanelHost) {
    this.addLabel(0, 0, 40, 0, 0, 0xFFFFFF, "Energy Width")
    this.energyWidth =
      this.addTextField(0, mc.fontRenderer.getStringWidth("Energy Width") + 10, 30, 40, 20)
    this.energyWidth.text = LiteModDRImprovement.mod.energyBarWidth.toString()

    this.addLabel(1, 0, 70, 0, 0, 0xFFFFFF, "Energy Offset")
    this.energyOffset =
      this.addTextField(1, mc.fontRenderer.getStringWidth("Energy Offset") + 10, 60, 40, 20)
    this.energyOffset.text = LiteModDRImprovement.mod.energyBarOffset.toString()

    this.addLabel(2, 0, 100, 0, 0, 0xFFFFFF, "Text X Offset")
    this.textXOffset =
      this.addTextField(2, mc.fontRenderer.getStringWidth("Text X Offset") + 10, 90, 40, 20)
    this.textXOffset.text = LiteModDRImprovement.mod.textXOffset.toString()

    this.addLabel(2, 0, 130, 0, 0, 0xFFFFFF, "Text Y Offset")
    this.textYOffset =
      this.addTextField(3, mc.fontRenderer.getStringWidth("Text Y Offset") + 10, 120, 40, 20)
    this.textYOffset.text = LiteModDRImprovement.mod.textYOffset.toString()

    this.addControl(
      GuiButton(
        1,
        0,
        150,
        "Display Health Bar: ${LiteModDRImprovement.mod.showHealthBar}"
      )
    ) {
      LiteModDRImprovement.mod.showHealthBar = !LiteModDRImprovement.mod.showHealthBar
      it.displayString = "Display Health Bar: ${LiteModDRImprovement.mod.showHealthBar}"
    }

    this.addControl(
      GuiButton(
        2,
        0,
        180,
        "Display Energy Bar: ${LiteModDRImprovement.mod.showEnergyBar}"
      )
    ) {
      LiteModDRImprovement.mod.showEnergyBar = !LiteModDRImprovement.mod.showEnergyBar
      it.displayString = "Display Energy Bar: ${LiteModDRImprovement.mod.showEnergyBar}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        0,
        "Interpolate Energy: ${LiteModDRImprovement.mod.interpolateEnergy}"
      )
    ) {
      LiteModDRImprovement.mod.interpolateEnergy = !LiteModDRImprovement.mod.interpolateEnergy
      it.displayString = "Interpolate Energy: ${LiteModDRImprovement.mod.interpolateEnergy}"
    }
  }
}
