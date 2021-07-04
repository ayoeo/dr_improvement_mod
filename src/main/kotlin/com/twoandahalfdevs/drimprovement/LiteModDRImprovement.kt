package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.ITextComponent
import java.io.File
import java.util.*
import kotlin.math.pow

val minecraft: Minecraft
  get() = Minecraft.getMinecraft()

var currentHealth = 0
var barMaxHealth = 0

//var fakeMaxHealth: Int? = 0
var clas = "???"
var xp = "???"

var healthBarUUID: UUID? = null;

@ExposableOptions(
  strategy = ConfigStrategy.Unversioned,
  filename = "dr_improvement_mod.json",
  aggressive = true
)
class LiteModDRImprovement : LiteMod, HUDRenderListener, Tickable, PacketHandler {
  override fun upgradeSettings(v: String?, c: File?, o: File?) {}

  override fun getHandledPackets(): MutableList<Class<out Packet<*>>> =
    mutableListOf(
      SPacketParticles::class.java,
      SPacketEffect::class.java,
      SPacketUpdateBossInfo::class.java
    )


  override fun handlePacket(netHandler: INetHandler?, packet: Packet<*>?): Boolean {
    if (minecraft.player == null) return true

    if (packet is SPacketUpdateBossInfo) {
      val text = packet.name?.unformattedText ?: return true

      if (text.startsWith("LV. ")) {
        healthBarUUID = packet.uniqueId;
      }

      // This is our health bar
      if (packet.uniqueId == healthBarUUID) {
        val split = text.split('-');
        clas = split[0].trim()
        val hp = split[1].trim()
        xp = split[2].trim()

        val hpTrimmed = hp.removePrefix("HP").replace(" ", "")
        val (current, max) = if (hpTrimmed.contains('/')) {
          val hpsplit = hpTrimmed.split('/')
          Pair(hpsplit[0].toInt(), hpsplit[1].toInt())
        } else {
          Pair(hpTrimmed.toInt(), hpTrimmed.toInt())
        }

        barMaxHealth = max

        if (minecraft.player.health == 1.0F) {
          // Can't rely on minecraft health below 1 health :(
          // Use the bad health bar
          currentHealth = current
        }
      }

      return true
    }

    if (packet is SPacketParticles) {
      if (
      // Elites and stuff idk
        packet.particleType == EnumParticleTypes.EXPLOSION_NORMAL
        || packet.particleType == EnumParticleTypes.EXPLOSION_LARGE
        || packet.particleType == EnumParticleTypes.EXPLOSION_HUGE

        // Fishing lmao
        || packet.particleType == EnumParticleTypes.WATER_WAKE
        || packet.particleType == EnumParticleTypes.WATER_SPLASH

      // For red
//        || packet.particleType == EnumParticleTypes.SWEEP_ATTACK
      ) {
        return true
      }
      return false
    } else if (packet is SPacketEffect) {
      // Block break effect
      if (packet.soundType == 2001) {
        // Thorns
        if (packet.soundData == 18) return false

        // Weird redstone blood thing
        if (packet.soundData == 152) return false
      }
    } else if (packet is SPacketEntityMetadata) {
      // TODO - update player health over head : o
    }

    return true
  }

  override fun onTick(
    minecraft: Minecraft?,
    partialTicks: Float,
    inGame: Boolean,
    clock: Boolean
  ) {
    if (minecraft?.player == null) {
      actionBarMsg = ""
      actionBarTime = 0
    }

    if (clock && minecraft?.player != null) {
      onTick()
    }
  }

  override fun onPreRenderHUD(screenWidth: Int, screenHeight: Int) {
    GlStateManager.pushMatrix()
    val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor.toDouble()
    val scale = scaleFactor / scaleFactor.pow(2.0)
    GlStateManager.scale(scale, scale, 1.0)

    `draw energy bar and also the health bar too don't forget`()

    GlStateManager.popMatrix()
  }

  override fun onPostRenderHUD(screenWidth: Int, screenHeight: Int) {
  }

  override fun getName(): String = "DR Improvement Mod"
  override fun getVersion(): String = "1.1"

  override fun init(configPath: File?) = Unit
}
