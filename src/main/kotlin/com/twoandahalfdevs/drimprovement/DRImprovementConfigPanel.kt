package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigPanelHost
import net.minecraft.client.gui.GuiButton

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
        3,
        0,
        210,
        "Only Change Fov On Sprint: ${LiteModDRImprovement.mod.onlySprintFov}"
      )
    ) {
      LiteModDRImprovement.mod.onlySprintFov = !LiteModDRImprovement.mod.onlySprintFov
      it.displayString = "Only Change Fov On Sprint: ${LiteModDRImprovement.mod.onlySprintFov}"
    }

    this.addControl(
      GuiButton(
        4,
        0,
        240,
        "Show Helpful Text: ${LiteModDRImprovement.mod.showHelpfulText}"
      )
    ) {
      LiteModDRImprovement.mod.showHelpfulText = !LiteModDRImprovement.mod.showHelpfulText
      it.displayString = "Show Helpful Text: ${LiteModDRImprovement.mod.showHelpfulText}"
    }

    this.addControl(
      GuiButton(
        5,
        0,
        270,
        "Disable Cooldown Hand Effect: ${LiteModDRImprovement.mod.removeCooldownHandEffect}"
      )
    ) {
      LiteModDRImprovement.mod.removeCooldownHandEffect =
        !LiteModDRImprovement.mod.removeCooldownHandEffect
      it.displayString =
        "Disable Cooldown Hand Effect: ${LiteModDRImprovement.mod.removeCooldownHandEffect}"
    }

    this.addControl(
      GuiButton(
        6,
        0,
        300,
        "Disable W Key Sprint: ${LiteModDRImprovement.mod.noWTapSprint}"
      )
    ) {
      LiteModDRImprovement.mod.noWTapSprint =
        !LiteModDRImprovement.mod.noWTapSprint
      it.displayString =
        "Disable W Key Sprint: ${LiteModDRImprovement.mod.noWTapSprint}"
    }

    this.addControl(
      GuiButton(
        7,
        0,
        330,
        "Hide Debug: ${LiteModDRImprovement.mod.hideDebug}"
      )
    ) {
      LiteModDRImprovement.mod.hideDebug =
        !LiteModDRImprovement.mod.hideDebug
      it.displayString =
        "Hide Debug: ${LiteModDRImprovement.mod.hideDebug}"
    }

    this.addControl(
      GuiButton(
        8,
        0,
        360,
        "Show Extra Lore: ${LiteModDRImprovement.mod.showExtraLore}"
      )
    ) {
      LiteModDRImprovement.mod.showExtraLore =
        !LiteModDRImprovement.mod.showExtraLore
      it.displayString =
        "Show Extra Lore: ${LiteModDRImprovement.mod.showExtraLore}"
    }

    this.addControl(
      GuiButton(
        9,
        0,
        390,
        "Limit Fps When Tabbed Out: ${LiteModDRImprovement.mod.limitFpsWhenTabbedOut}"
      )
    ) {
      LiteModDRImprovement.mod.limitFpsWhenTabbedOut =
        !LiteModDRImprovement.mod.limitFpsWhenTabbedOut
      it.displayString =
        "Limit Fps When Tabbed Out: ${LiteModDRImprovement.mod.limitFpsWhenTabbedOut}"
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

    this.addControl(
      GuiButton(
        0,
        0,
        420,
        "Threaded Input: ${LiteModDRImprovement.mod.threadedMouseInput}"
      )
    ) {
      LiteModDRImprovement.mod.threadedMouseInput = !LiteModDRImprovement.mod.threadedMouseInput
      it.displayString = "Threaded Input: ${LiteModDRImprovement.mod.threadedMouseInput}"
    }

    val button = GuiButton(
      0,
      0,
      450,
      "Rapid Mouse Polling: ${LiteModDRImprovement.mod.rapidMousePolling} (req. threaded input) (trade 1 cpu core for <0.5ms latency lmao I'm turning it on idk about you)"
    )
    button.setWidth(640)
    this.addControl(button) {
      LiteModDRImprovement.mod.rapidMousePolling = !LiteModDRImprovement.mod.rapidMousePolling
      it.displayString =
        "Rapid Mouse Polling: ${LiteModDRImprovement.mod.rapidMousePolling}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        480,
        "Creative Mode Look: ${LiteModDRImprovement.mod.creativeModeLook}"
      )
    ) {
      LiteModDRImprovement.mod.creativeModeLook =
        !LiteModDRImprovement.mod.creativeModeLook
      it.displayString =
        "Creative Mode Look: ${LiteModDRImprovement.mod.creativeModeLook}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        510,
        "Prevent Hotbar Scrolling: ${LiteModDRImprovement.mod.preventHotbarScrolling}"
      )
    ) {
      LiteModDRImprovement.mod.preventHotbarScrolling =
        !LiteModDRImprovement.mod.preventHotbarScrolling
      it.displayString =
        "Prevent Hotbar Scrolling: ${LiteModDRImprovement.mod.preventHotbarScrolling}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        540,
        "Show Flame Particles: ${LiteModDRImprovement.mod.showFlameParticles}"
      )
    ) {
      LiteModDRImprovement.mod.showFlameParticles =
        !LiteModDRImprovement.mod.showFlameParticles
      it.displayString =
        "Show Flame Particles: ${LiteModDRImprovement.mod.showFlameParticles}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        570,
        "Show Chest Particles: ${LiteModDRImprovement.mod.showChestParticles}"
      )
    ) {
      LiteModDRImprovement.mod.showChestParticles =
        !LiteModDRImprovement.mod.showChestParticles
      it.displayString =
        "Show Chest Particles: ${LiteModDRImprovement.mod.showChestParticles}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        600,
        "Show Particles: ${LiteModDRImprovement.mod.showParticles}"
      )
    ) {
      LiteModDRImprovement.mod.showParticles =
        !LiteModDRImprovement.mod.showParticles
      it.displayString =
        "Show Particles: ${LiteModDRImprovement.mod.showParticles}"
    }

    this.addControl(
      GuiButton(
        0,
        0,
        630,
        "No Hurt Cam: ${LiteModDRImprovement.mod.noHurtCam}"
      )
    ) {
      LiteModDRImprovement.mod.noHurtCam =
        !LiteModDRImprovement.mod.noHurtCam
      it.displayString =
        "No Hurt Cam: ${LiteModDRImprovement.mod.noHurtCam}"
    }
  }
}
