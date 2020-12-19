package com.twoandahalfdevs.drimprovement

import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.OpenGlHelper.glUseProgram
import net.minecraft.client.shader.ShaderLoader
import org.lwjgl.opengl.GL20

fun ShaderLoader.shader(): Int {
  val shaderField = this::class.java.declaredFields.first { it.type == Integer.TYPE }
  shaderField.isAccessible = true
  return shaderField.getInt(this)
}

var lastExperience: Float = 0f

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

private val colourEnergy = GLUniform.Colour(GL20.glGetUniformLocation(program, "colourEnergy"))

private val circleCenter = GLUniform.Vec2(GL20.glGetUniformLocation(program, "circleCenter"))

private val circleRadius = GLUniform.Float(GL20.glGetUniformLocation(program, "circleRadius"))

private val resolution = GLUniform.Vec2(GL20.glGetUniformLocation(program, "resolution"))

private val energyPercent = GLUniform.Float(GL20.glGetUniformLocation(program, "energyPercent"))

fun onTick() {
  lastExperience = minecraft.player?.experience ?: 0f
}

fun drawEnergyBar(partialTicks: Float) {
  val radius = 30
  glUseProgram(program)

  val currentExp = minecraft.player?.experience ?: 0f
  val interpolatedExperience = lastExperience + (currentExp - lastExperience) * partialTicks
  energyPercent.set(interpolatedExperience)
  resolution.set(displayWidth.toFloat(), displayHeight.toFloat())

  // TODO - configuration
  circleRadius.set(radius.toFloat())
  colourBase.set(Colour.hex(0xA83232))
  colourEnergy.set(Colour.hex(0x32A856))

  val (centerX, centerY) = Pair(displayWidth / 2, displayHeight / 2)
  circleCenter.set(centerX.toFloat(), centerY.toFloat())
  blitScreenImage()

  glUseProgram(0)
}
