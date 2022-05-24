package com.twoandahalfdevs.drimprovement

import com.mumfrey.liteloader.gl.GL
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL20

class Colour private constructor(var r: Float, var g: Float, var b: Float, var a: Float = 1f) {
  companion object {
    fun hex(hex: Long): Colour {
      val r = (hex shr 16 and 255).toFloat() / 255f
      val g = (hex shr 8 and 255).toFloat() / 255f
      val b = (hex and 255).toFloat() / 255f
      var a = (hex shr 24 and 255).toFloat() / 255f
      a = if (a == 0f) {
        1f
      } else {
        a
      }
      return Colour(r, g, b, a)
    }
  }
}

sealed class GLUniform(val index: Int) {
  class Colour(index: Int) : GLUniform(index) {
    fun set(colour: com.twoandahalfdevs.drimprovement.Colour) {
      GL20.glUniform4f(this.index, colour.r, colour.g, colour.b, colour.a)
    }
  }

  class Float(index: Int) : GLUniform(index) {
    fun set(value: kotlin.Float) {
      GL20.glUniform1f(this.index, value)
    }
  }

  class Vec2(index: Int) : GLUniform(index) {
    fun set(x: kotlin.Float, y: kotlin.Float) {
      GL20.glUniform2f(this.index, x, y)
    }
  }
}

fun blitScreenImage() {
  renderTexturedRect(0, 0, displayWidth, displayHeight)
}

fun renderTexturedRect(x: Int, y: Int, width: Int, height: Int) {
  GL.glDisableLighting()
  GL.glEnableBlend()

  val tess = Tessellator.getInstance()
  val buffer = tess.buffer
  buffer.begin(GL.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
  buffer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 0.0).endVertex()
  buffer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 0.0).endVertex()
  buffer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 1.0).endVertex()
  buffer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 1.0).endVertex()
  tess.draw()
}
