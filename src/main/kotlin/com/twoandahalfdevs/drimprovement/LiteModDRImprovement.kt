package com.twoandahalfdevs.drimprovement

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mojang.realmsclient.gui.ChatFormatting
import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.core.LiteLoaderEventBroker
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import com.twoandahalfdevs.drimprovement.LiteModDRImprovement.Companion.mod
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.monster.EntitySpellcasterIllager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.client.CPacketTabComplete
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MouseHelper
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import org.lwjgl.input.Keyboard
import sun.audio.AudioPlayer.player
import java.io.File
import java.lang.reflect.Constructor
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

val minecraft: Minecraft
  get() = Minecraft.getMinecraft()

var currentHealth = 0
var barMaxHealth = 0

//var fakeMaxHealth: Int? = 0
var clas = "???"
var xp = "???"

var healthBarUUID: UUID? = null

private val abilityreg = """(.*) has activated The Fast""".toRegex()
private val debugDmg = """(?:[0-9]+ DMG -> (.+) \[[0-9]+ HP])|(?:-[0-9]+ HP \((.+)\))""".toRegex()
private val otherStuff = listOf("THORNS", "IN FIRE", "ON FIRE", "LAVA", "FALL", "SUFFOCATION")

// TODO - what if you don't have the whole tree lmfao idk it's FUCKED IT'S ALLL FUCKED WHAT DO I DO
private const val combatBonusTime = 6.5

val hideDebugKeybind = KeyBinding("Show/Hide Debug", Keyboard.KEY_O, "Dr Improvement Mod");

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
  @SerializedName("creative_mode_look")
  var creativeModeLook = false

  @Expose
  @SerializedName("show_helpful_text")
  var showHelpfulText = true

  @Expose
  @SerializedName("no_w_tap_sprint")
  var noWTapSprint = false

  @Expose
  @SerializedName("hide_mob_debug")
  var hideDebug = false

  @Expose
  @SerializedName("show_extra_lore")
  var showExtraLore = true

  @Expose
  @SerializedName("limit_fps")
  var limitFpsWhenTabbedOut = true

  @Expose
  @SerializedName("rapid_mouse_polling")
  var rapidMousePolling = false

  @Expose
  @SerializedName("prevent_hotbar_scrolling")
  var preventHotbarScrolling = false

  @Expose
  @SerializedName("threaded_mouse_input")
  var threadedMouseInput = false

  companion object {
    lateinit var mod: LiteModDRImprovement
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
      lastUpdatedBonusTime = Minecraft.getSystemTime()
      bonus = (20 * combatBonusTime).roundToInt()
    }

    // TODO - better check for monsters/players
    val dmgMatches = debugDmg.find(msg)
    val attacked = dmgMatches?.groupValues?.getOrNull(1)
    val attacker = dmgMatches?.groupValues?.getOrNull(2)
    val probablyCombatTimer =
      ((combatTimer.toDouble() / 20.0) - (Minecraft.getSystemTime() - lastupdatedCombatTime) / 1000.0).coerceAtLeast(
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
          lastUpdatedBonusTime = Minecraft.getSystemTime()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = Minecraft.getSystemTime()
        combatTimer = (20 * combatPvPTime).roundToInt()
      } else {
        // COMBAT BONUS WHOAHHO
        if (probablyCombatTimer <= 0.0) {
          lastUpdatedBonusTime = Minecraft.getSystemTime()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = Minecraft.getSystemTime()
        combatTimer = (20 * combatPveTime).roundToInt()

//        if (hideDebug) { // todo - setting debug mob
//          return false
//        }
      }
    } else if (attacker != null && attacker.isNotEmpty()) {
      // MONSTER??
      if (!otherStuff.any { it == attacker }) {
        // COMBAT BONUS WHOAHHO
        if (probablyCombatTimer <= 0.0) {
          lastUpdatedBonusTime = Minecraft.getSystemTime()
          bonus = (20 * combatBonusTime).roundToInt()
        }

        lastupdatedCombatTime = Minecraft.getSystemTime()
        combatTimer =
          if (attacker.contains(' '))
            (20 * combatPveTime).roundToInt()
          else
            (20 * combatPvPTime).roundToInt()

//        if (hideDebug) { // todo - setting debug mob
//          return false
//        }
      }
    }
    return true
  }

  override fun getConfigPanelClass(): Class<out ConfigPanel> {
    return DRImprovementConfigPanel::class.java
  }

  val debugRegex =
    Regex("""^\+[0-9]+ HP.*\[[0-9]+ HP]| *\*.*\*|[0-9]+ DMG -> .*\[[0-9]+ HP]|-[0-9]+ HP \(.*\) \[[0-9]+ HP]|-[0-9]+ HP \(.*\) \[[0-9]+]| *\* OPPONENT .*\*| *Drained Energy .*\*|Your Gem Find resulted in gems.| *\+ [0-9]+ EXP.*\[.*]| *\+[0-9]+G| *\+[0-9]+ (?:Logs|Planks|Clay|Ceramic|Cobblestone|Stone Brick|Grain|Bread)|You did not have room for [0-9]+ .*, so they were discarded.|\+.*|-.*""")

  override fun getHandledPackets(): MutableList<Class<out Packet<*>>> =
    mutableListOf(
      SPacketParticles::class.java,
      SPacketEffect::class.java,
      SPacketUpdateBossInfo::class.java,
      SPacketUpdateScore::class.java,
      SPacketEntityMetadata::class.java,
      SPacketTabComplete::class.java,
      SPacketTitle::class.java,
      SPacketChat::class.java
    )

  private var scoreWasUpdated = mutableMapOf<String, Int>()
  private var latestCurrentHealth = mutableMapOf<String, Float>()

  private var maxHealthValues = mutableMapOf<String, Int>()

  data class UpdateInfo(var goodToUpdate: Boolean, val freshHealth: Int)

  private var lastExp = 1f
  override fun handlePacket(netHandler: INetHandler?, packet: Packet<*>?): Boolean {
    if (minecraft.player == null) return true
//    if (packet is SPacketSetExperience) {
//      val exp = packet.experienceBar
//      val usedExp = lastExp - exp
//      if (exp < lastExp) {
//        println("Used: $usedExp")
//      }
//      lastExp = exp
//      return true
//    }

    if (packet is SPacketUpdateScore) {
      if (packet.objectiveName != "health") {
        return true
      }

      scoreWasUpdated[packet.playerName] = packet.scoreValue

      // In case the player is under 5% health, we need to display SOME info
      val player = minecraft.world.playerEntities.find { it.name == packet.playerName }
      val maxHealth = maxHealthValues[packet.playerName]
      if (maxHealth != null) {
        player?.health = (packet.scoreValue.toFloat() * 20f) / maxHealth.toFloat()
      }

      // We have to wait for an update haha
      latestCurrentHealth.remove(packet.playerName)

      // TODO - just update the score when we get some debug haha
      return false
    }

    if (packet is SPacketUpdateBossInfo) {
      val text = packet.name?.unformattedText ?: return true

      if (text.startsWith("LV. ")) {
        healthBarUUID = packet.uniqueId
      }

      // This is our health bar
      if (packet.uniqueId == healthBarUUID) {
        val split = text.split('-')
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
      if (player is EntityPlayer) {
        val packetHealth = packet.dataManagerEntries.find { it.key.id == 7 }?.value as Float?
        // 1.0 isn't a real health it's just like nothing like it means literally nothing it just means
        //  they're below 5% health it's useless I swear to god I hate this game it doesn't make any sense I just want it to make sense
        if (packetHealth != null && packetHealth != 1.0f) {
          // Update the health now : )
          latestCurrentHealth[player.name] = packetHealth
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

    if (inGame && minecraft?.currentScreen == null) {
      if (hideDebugKeybind.isPressed) {
        hideDebug = !hideDebug
        LiteLoader.getInstance().writeConfig(mod)
        minecraft?.ingameGUI?.addChatMessage(
          ChatType.CHAT,
          TextComponentString("§7[Debug]: ${if (hideDebug) "§cHidden" else "§aShown"}§7.")
        )
      }
    }

    if (clock && minecraft?.player != null) {
      onTick()

      // Update health
      scoreWasUpdated = scoreWasUpdated.filter { (name, score) ->
        val player = minecraft.world.getPlayerEntityByName(name)
        val latestCurrentHealth = latestCurrentHealth[name]
        if (player != null && latestCurrentHealth != null) {
          val ratio = player.maxHealth / latestCurrentHealth
          val maxHealth = score.toDouble() * ratio
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

  @Throws(ReflectiveOperationException::class)
  private fun createDefaultEnvironment(): ControllerEnvironment? {
    val constructor: Constructor<ControllerEnvironment> =
      Class.forName("net.java.games.input.DefaultControllerEnvironment")
        .declaredConstructors[0] as Constructor<ControllerEnvironment>
    constructor.isAccessible = true

    return constructor.newInstance()
  }

  var dx = 0
  var dy = 0
  private val mice = mutableListOf<Mouse>()
  val pollingSemaphore = Semaphore(1)

  private var wasThreaded = threadedMouseInput

  override fun init(configPath: File?) {
    mod = this

    LiteLoader.getInput().registerKeyBinding(hideDebugKeybind)

    Minecraft.getMinecraft().mouseHelper = if (threadedMouseInput) {
      RawMouseHelper()
    } else {
      MouseHelper()
    }

    val inputThread = Thread {
      var missedMoves = 0
      while (true) {
        if (wasThreaded && !threadedMouseInput) {
          minecraft.mouseHelper = MouseHelper()
        } else if (!wasThreaded && threadedMouseInput) {
          minecraft.mouseHelper = RawMouseHelper()
        }
        wasThreaded = threadedMouseInput

        if (!threadedMouseInput) {
          Thread.sleep(100)
          continue
        }

        if (mice.isEmpty()) {
          missedMoves = 0
          try {
            val controllers: Array<Controller> = createDefaultEnvironment()!!.controllers
            for (controller in controllers.filter { it.type == Controller.Type.MOUSE }) {
              mice.add(controller as Mouse)
            }
          } catch (ignored: ReflectiveOperationException) {
          }
        } else {
          org.lwjgl.input.Mouse.poll()
          val couldHaveMissed =
            org.lwjgl.input.Mouse.getDX() != 0 || org.lwjgl.input.Mouse.getDY() != 0
          var missed = true


          for (mouse in mice.toList()) {
            pollingSemaphore.acquire()
            if (mouse.poll()) {
              val x = mouse.x.pollData.toInt()
              val y = mouse.y.pollData.toInt()
              if (x != 0 || y != 0) {
                missed = false
              }
              if (minecraft.inGameHasFocus) {
                dx += x
                dy += y
              }
            } else {
              mice.clear()
            }
            pollingSemaphore.release()
          }

          if (!missed && couldHaveMissed) {
            missedMoves = 0
          }

          if (missed && couldHaveMissed) {
            missedMoves += 1
          }

          if (missedMoves > 1000) {
            mice.clear()
          }
        }

        // Now we wait (or do we)
        if (!rapidMousePolling) {
          Thread.sleep(1)
        }
      }
    }
    inputThread.name = "inputThread"
    inputThread.priority = Thread.MAX_PRIORITY
    inputThread.start()
  }
}
