package com.twoandahalfdevs.drimprovement

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.core.LiteLoaderEventBroker
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.text.ITextComponent
import sun.audio.AudioPlayer.player
import java.io.File
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

val minecraft: Minecraft
  get() = Minecraft.getMinecraft()

var currentHealth = 0
var barMaxHealth = 0

//var fakeMaxHealth: Int? = 0
var clas = "???"
var xp = "???"

var healthBarUUID: UUID? = null;

private val abilityreg = """(.*) has activated The Fast""".toRegex()
private val debugDmg = """(?:[0-9]+ DMG -> (.+) \[[0-9]+ HP])|(?:-[0-9]+ HP \((.+)\))""".toRegex()
private val otherStuff = listOf("THORNS", "IN FIRE", "ON FIRE", "LAVA", "FALL", "SUFFOCATION")

// TODO - what if you don't have the whole tree lmfao idk it's FUCKED IT'S ALLL FUCKED WHAT DO I DO
private const val combatBonusTime = 6.5
//private const val combatPveTimeRog = 5.2

private val combatPveTime: Double
  get() = if (clas.contains("Rogue")) 5.2 else 8.0

private val combatPvPTime: Double
  get() = if (clas.contains("Rogue")) 9.75 else 15.0

@ExposableOptions(
  strategy = ConfigStrategy.Unversioned,
  filename = "dr_improvement_mod.json",
  aggressive = true
)
class LiteModDRImprovement : LiteMod, HUDRenderListener, Tickable, PacketHandler, ChatFilter,
  Configurable {

  @Expose
  @SerializedName("energy_bar_width")
  var energyBarWidth = 20.0f

  @Expose
  @SerializedName("energy_bar_offset")
  var energyBarOffset = 0.16f

  @Expose
  @SerializedName("textXOffset")
  var textXOffset = 25

  @Expose
  @SerializedName("textYOffset")
  var textYOffset = 30

  @Expose
  @SerializedName("interpolate_energy")
  var interpolateEnergy = true

  @Expose
  @SerializedName("show_health")
  var showHealthBar = true

  @Expose
  @SerializedName("only_sprint_fov")
  var onlySprintFov = false

  @Expose
  @SerializedName("remove_cooldown_hand_effect")
  var removeCooldownHandEffect = true

  @Expose
  @SerializedName("show_energy")
  var showEnergyBar = true

  @Expose
  @SerializedName("show_helpful_text")
  var showHelpfulText = true

  @Expose
  @SerializedName("no_w_tap_sprint")
  var noWTapSprint = false

  @Expose
  @SerializedName("hide_mob_debug")
  var hideMobDebug = false

  @Expose
  @SerializedName("show_extra_lore")
  var showExtraLore = true

  @Expose
  @SerializedName("limit_fps")
  var limitFpsWhenTabbedOut = true

  companion object {
    lateinit var mod: LiteModDRImprovement
  }

  init {
    mod = this
  }

  override fun upgradeSettings(v: String?, c: File?, o: File?) {}

  override fun onChat(
    chat: ITextComponent,
    message: String,
    newMessage: LiteLoaderEventBroker.ReturnValue<ITextComponent>?
  ): Boolean {
    val msg = chat.unformattedText
    val abilmatches = abilityreg.find(msg)

    val matches = abilmatches?.groupValues?.getOrNull(1)
    if (matches?.endsWith(minecraft.player.name) == true) {
      lastUpdatedBonusTime = System.currentTimeMillis()
      bonus = (20 * combatBonusTime).roundToInt()
    }

    // TODO - better check for monsters/players
    val dmgMatches = debugDmg.find(msg)
    val attacked = dmgMatches?.groupValues?.getOrNull(1)
    val attacker = dmgMatches?.groupValues?.getOrNull(2)
    val probablyCombatTimer =
      ((combatTimer.toDouble() / 20.0) - (System.currentTimeMillis() - lastupdatedCombatTime) / 1000.0).coerceAtLeast(
        0.0
      )

    if (attacked != null && attacked.isNotEmpty()) {
      if (attacked.contains("""\[.+]""".toRegex()) || (minecraft.world.playerEntities.any {
          attacked == it.name
        }) || attacked.contains(""" (?:S|S\+|S\+\+|GD|LORE|GM|PMOD) """.toRegex()) || !attacked.contains(
          ' '
        )
      ) {
        // Player moment
        // COMBAT BONUS WHOAHHO
        if (probablyCombatTimer <= 0.0) {
          lastUpdatedBonusTime = System.currentTimeMillis()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = System.currentTimeMillis()
        combatTimer = (20 * combatPvPTime).roundToInt()
      } else {
        // COMBAT BONUS WHOAHHO
        if (probablyCombatTimer <= 0.0) {
          lastUpdatedBonusTime = System.currentTimeMillis()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = System.currentTimeMillis()
        combatTimer = (20 * combatPveTime).roundToInt()

        if (hideMobDebug) { // todo - setting debug mob
          return false
        }
      }
    } else if (attacker != null && attacker.isNotEmpty()) {
      // MONSTER??
      if (!otherStuff.any { it == attacker }) {
        // COMBAT BONUS WHOAHHO
        if (probablyCombatTimer <= 0.0) {
          lastUpdatedBonusTime = System.currentTimeMillis()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = System.currentTimeMillis()
        combatTimer =
          if (attacker.contains(' '))
            (20 * combatPveTime).roundToInt()
          else
            (20 * combatPvPTime).roundToInt()

        if (hideMobDebug) { // todo - setting debug mob
          return false
        }
      }
    }
    return true
  }

  override fun getConfigPanelClass(): Class<out ConfigPanel> {
    return DRImprovementConfigPanel::class.java
  }

  override fun getHandledPackets(): MutableList<Class<out Packet<*>>> =
    mutableListOf(
      SPacketParticles::class.java,
      SPacketEffect::class.java,
      SPacketUpdateBossInfo::class.java,
      SPacketUpdateScore::class.java,
      SPacketEntityMetadata::class.java
    )

  private var needsHealthUpdate = mutableMapOf<String, UpdateInfo>()
  private var maxHealthValues = mutableMapOf<String, Int>()

  data class UpdateInfo(var goodToUpdate: Boolean, val freshHealth: Int)

  override fun handlePacket(netHandler: INetHandler?, packet: Packet<*>?): Boolean {
    if (minecraft.player == null) return true

    if (packet is SPacketUpdateScore) {
      if (packet.objectiveName != "health") {
        return true
      }

      needsHealthUpdate[packet.playerName] = UpdateInfo(false, packet.scoreValue)
      return false
    }

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
      val player = minecraft.world.getEntityByID(packet.entityId)
      if (player != null) {
        needsHealthUpdate[player.name]?.let {
          it.goodToUpdate = true
        }
      }
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

      // Update health
      needsHealthUpdate = needsHealthUpdate.filter { (name, updateInfo) ->
        val player = minecraft.world.getPlayerEntityByName(name)
        if (player != null && updateInfo.goodToUpdate) {
          if (player.health > 20.0) {
            println("Abil: ${player.health}, ${player.maxHealth}, ${updateInfo.freshHealth}")
          }
          val ratio = player.maxHealth / player.health
          val maxHealth = updateInfo.freshHealth.toDouble() * ratio
          maxHealthValues[player.name] = maxHealth.roundToInt()
          false
        } else {
          true
        }
      }.toMutableMap()

      // Update the scoreboard to reflect real health values
      minecraft.world.scoreboard.getObjective("health")
        ?.scoreboard
        ?.scores
        ?.forEach {
          maxHealthValues[it.playerName]?.let { maxHealth ->
            val player = minecraft.world.getPlayerEntityByName(it.playerName)
            if (player != null) {
              val ratio = player.health / player.maxHealth
              it.scorePoints = (maxHealth.toDouble() * ratio).roundToInt()
            }
          }
        }
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
