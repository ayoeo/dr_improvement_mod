package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.core.runtime.Obf.Minecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.OpenGlHelper.glUseProgram
import net.minecraft.client.shader.ShaderLoader
import net.minecraft.item.Item
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.glUseProgram
import java.lang.NumberFormatException
import java.math.RoundingMode
import kotlin.math.roundToInt

fun ShaderLoader.shader(): Int {
  val shaderField = this::class.java.declaredFields.first { it.type == Integer.TYPE }
  shaderField.isAccessible = true
  return shaderField.getInt(this)
}

var lastHpPercent: Float = 0f
var hpPercent: Float = 0f

var lastExperience: Float = 0f
var currentExp: Float = 0f

val program = run {
  val vert = ShaderLoader.loadShader(
    minecraft.resourceManager,
    ShaderLoader.ShaderType.VERTEX,
    "gui"
  )
  val frag = ShaderLoader.loadShader(
    minecraft.resourceManager,
    ShaderLoader.ShaderType.FRAGMENT,
    "gui"
  )

  val program = OpenGlHelper.glCreateProgram()
  OpenGlHelper.glAttachShader(program, vert.shader())
  OpenGlHelper.glAttachShader(program, frag.shader())
  OpenGlHelper.glLinkProgram(program)

  program
}

val displayWidth
  get() = minecraft.displayWidth
val displayHeight
  get() = minecraft.displayHeight

private val colourBase = GLUniform.Colour(GL20.glGetUniformLocation(program, "colourBase"))

private val colourHpBase = GLUniform.Colour(GL20.glGetUniformLocation(program, "colourHpBase"))

private val colourHp = GLUniform.Colour(GL20.glGetUniformLocation(program, "colourHp"))

private val colourEnergy = GLUniform.Colour(GL20.glGetUniformLocation(program, "colourEnergy"))

private val circleCenter = GLUniform.Vec2(GL20.glGetUniformLocation(program, "circleCenter"))

private val healthPercent = GLUniform.Float(GL20.glGetUniformLocation(program, "healthPercent"))

private val resolution = GLUniform.Vec2(GL20.glGetUniformLocation(program, "resolution"))

private val energyPercent = GLUniform.Float(GL20.glGetUniformLocation(program, "energyPercent"))

private var lastTick = Minecraft.getSystemTime()

var maxHealth = 0

fun onTick() {
  if (minecraft.player.maxHealth <= 20.0) {
    // Only update our max health if we're not in some shield ability (WAR / PAL)
    maxHealth = barMaxHealth
  }

  if (currentExp == 0f) currentExp = minecraft.player?.experience ?: 0f
  lastExperience = currentExp
  currentExp = minecraft.player?.experience ?: 0f

  lastHpPercent = hpPercent
  hpPercent = (minecraft.player?.health ?: 0f) / 20f

  lastTick = Minecraft.getSystemTime()
}

var actionBarMsg = ""
var actionBarTime = 0

private var cd = 0
private var lastUpdatedCdTime = System.currentTimeMillis()
var bonus = 0
var lastUpdatedBonusTime = System.currentTimeMillis()
var lastupdatedCombatTime = System.currentTimeMillis()

private var pots = 10
private var totalPots = 0
var combatTimer = 0

private val cdreg = """Cooldown: \[(?:([0-9]*)m)? ?(?:([0-9]*)s)?]""".toRegex()
private val potreg = """\[([0-9]*)/10] Potions""".toRegex()
private val combreg = """ Combat: \[([0-9]*)s]""".toRegex()

fun `draw energy bar and also the health bar too don't forget`() {
  if (minecraft.player.health != 1.0F) {
    currentHealth = (hpPercent * maxHealth.toFloat()).roundToInt()
  }
  val mrrhuahahhhaul = "§e$clas §f| §3$currentHealth / $maxHealth §f| §5$xp"
  val lenn = (minecraft.fontRenderer.getStringWidth(mrrhuahahhhaul)) / 2

  GlStateManager.pushMatrix()
  GlStateManager.scale(2.0, 2.0, 1.0)
  val xcenter = minecraft.displayWidth / 4
  val ycenter = minecraft.displayHeight / 4

  minecraft.fontRenderer.drawStringWithShadow(
    mrrhuahahhhaul,
    xcenter.toFloat() - lenn,
    4f,
    0xFFFFFF
  )

  // Cooldown
  if (actionBarTime > 0) {
    val cdMatches = cdreg.find(actionBarMsg)
    val potMatches = potreg.find(actionBarMsg)
    if (cdMatches != null) {
      try {
        val min = cdMatches.groupValues.getOrNull(1)
        val sec = cdMatches.groupValues.getOrNull(2)
        val minutes = if (min != null && min.isNotEmpty()) min.toInt() else 0
        val seconds = if (sec != null && sec.isNotEmpty()) sec.toInt() else 0
        cd = minutes * 60 + seconds
        lastUpdatedCdTime = System.currentTimeMillis() - ((60 - actionBarTime) * 50)
      } catch (e: NumberFormatException) {
      }
    }
    if (potMatches != null) {
      pots = potMatches.groupValues.getOrNull(1)?.toInt() ?: 0
    }

    val combatMatches = combreg.find(actionBarMsg)

    if (combatMatches != null) {
      var vrooom: Int? = null
      for (m in combatMatches.groupValues) {
        try {
          vrooom = m.toInt().coerceAtLeast(vrooom ?: 0)
        } catch (e: NumberFormatException) {
        }
      }
      if (vrooom != null) {
//        combatTimer = vrooom
//        lastupdatedCombatTime = System.currentTimeMillis() - ((60 - actionBarTime) * 50)
      }
    }
  }

  totalPots = minecraft.player.inventory.mainInventory.count {
    Item.getIdFromItem(it.item) == 373 || Item.getIdFromItem(it.item) == 438
  }

  val probablyTheCoolDownNow =
    (cd - (System.currentTimeMillis() - lastUpdatedCdTime) / 1000).coerceAtLeast(0)

  val probablyCombatTimer =
    ((combatTimer.toDouble() / 20.0) - (System.currentTimeMillis() - lastupdatedCombatTime) / 1000.0).coerceAtLeast(
      0.0
    )

  val probablyBonusTimer =
    ((bonus.toDouble() / 20.0) - (System.currentTimeMillis() - lastUpdatedBonusTime) / 1000.0).coerceAtLeast(
      0.0
    )

  val cdStr = if (probablyTheCoolDownNow > 0) "§c${probablyTheCoolDownNow}s" else "§aReady"
  minecraft.fontRenderer.drawStringWithShadow(
    cdStr,
    xcenter.toFloat() - minecraft.fontRenderer.getStringWidth(cdStr) / 2 - 25,
    ycenter.toFloat() - 28,
    0xFFFFFF
  )
  var color = "§c"
  val usablePots = pots.coerceAtMost(totalPots)
  if (usablePots > 6) color = "§a"
  else if (usablePots > 3) color = "§e"
  val potsStr = "${color}$usablePots / $totalPots"
  minecraft.fontRenderer.drawStringWithShadow(
    potsStr,
    xcenter.toFloat() - minecraft.fontRenderer.getStringWidth(potsStr) / 2 + 25,
    ycenter.toFloat() - 28,
    0xFFFFFF
  )

  val combatstr = if (probablyCombatTimer > 0) "§c${
    probablyCombatTimer.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
  }s" else "§a:)"
  minecraft.fontRenderer.drawStringWithShadow(
    combatstr,
    xcenter.toFloat() - minecraft.fontRenderer.getStringWidth(combatstr) / 2 + (if (clas.contains("Rogue")) 17 else 0),
    ycenter.toFloat() + 28,
    0xFFFFFF
  )

  val bonusStr =
    if (probablyCombatTimer <= 0) {
      "§a(:"
    } else {
      if (probablyBonusTimer > 0) "§a${
        probablyBonusTimer.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
      }s" else "§c):"
    }

  if (clas.contains("Rogue")) {
    minecraft.fontRenderer.drawStringWithShadow(
      bonusStr,
      xcenter.toFloat() - minecraft.fontRenderer.getStringWidth(combatstr) / 2 - 17,
      ycenter.toFloat() + 28,
      0xFFFFFF
    )
  }

  val okFood = minecraft.player.foodStats.foodLevel
  color = "§c"
  if (okFood > 15) color = "§a"
  else if (okFood > 10) color = "§e"
  minecraft.fontRenderer.drawStringWithShadow(
    "§rHunger: ${color}${minecraft.player.foodStats.foodLevel}",
    3f,
    12f,
    0xFFFFFF
  )
  GlStateManager.popMatrix()

  val partial = ((Minecraft.getSystemTime() - lastTick) / 50f).coerceAtMost(1f)

  glUseProgram(program)

  val interpolatedExperience = lastExperience + (currentExp - lastExperience) * partial
  energyPercent.set(interpolatedExperience)
  resolution.set(displayWidth.toFloat(), displayHeight.toFloat())

// TODO - configuration
  colourBase.set(Colour.hex(0xB8291F))
  colourEnergy.set(Colour.hex(0x21DB87))

  colourHpBase.set(Colour.hex(0xC70000))
  colourHp.set(Colour.hex(0x2DD248))

  val interpolatedHP = lastHpPercent + (hpPercent - lastHpPercent) * partial
  healthPercent.set(interpolatedHP)

  val (centerX, centerY) = Pair(displayWidth / 2, displayHeight / 2)
  circleCenter.set(centerX.toFloat(), centerY.toFloat())

  blitScreenImage()
  glUseProgram(0)
}
