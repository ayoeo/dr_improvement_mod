package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.HUDRenderListener
import com.mumfrey.liteloader.LiteMod
import com.mumfrey.liteloader.Tickable
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import java.io.File
import kotlin.math.pow

val minecraft: Minecraft
  get() = Minecraft.getMinecraft()

@ExposableOptions(
  strategy = ConfigStrategy.Unversioned,
  filename = "dr_improvement_mod.json",
  aggressive = true
)
class LiteModCool : LiteMod, HUDRenderListener, Tickable {
  override fun upgradeSettings(v: String?, c: File?, o: File?) {}

  override fun onTick(minecraft: Minecraft?, partialTicks: Float, inGame: Boolean, clock: Boolean) {
    if (clock) {
      onTick()
    }
  }

  override fun onPreRenderHUD(screenWidth: Int, screenHeight: Int) = Unit

  override fun onPostRenderHUD(screenWidth: Int, screenHeight: Int) {
    GlStateManager.pushMatrix()
    val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor.toDouble()
    val scale = scaleFactor / scaleFactor.pow(2.0)
    GlStateManager.scale(scale, scale, 1.0)

    drawEnergyBar(minecraft.renderPartialTicks)

    GlStateManager.popMatrix()
  }

  override fun getName(): String = "DR Improvement Mod"
  override fun getVersion(): String = "1.0"

  override fun init(configPath: File?) {
    // Dynamically load mods
  }
}
