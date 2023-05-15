package com.beanz.drpings

import com.mojang.realmsclient.client.Ping
import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.core.LiteLoaderEventBroker
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import javafx.geometry.Pos
import net.java.games.input.ControllerEnvironment
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketClientSettings
import net.minecraft.network.play.server.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import org.lwjgl.BufferUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.io.File
import java.lang.Math.*
import java.lang.reflect.Constructor


val minecraft: Minecraft
    get() = Minecraft.getMinecraft()

var pings = ArrayList<PingThing>()
private val locationPing = """.*\^ABC\%D.*""".toRegex()
val pingKeybind = KeyBinding("Ping location", Keyboard.KEY_V, "Dr Ping Mod");

@ExposableOptions(
    strategy = ConfigStrategy.Unversioned,
    filename = "dr_pings.json",
    aggressive = true
)
class LiteModDRPings : LiteMod, HUDRenderListener, Tickable, PacketHandler, ChatFilter,
    Configurable {

/*
  @Expose
  @SerializedName("show_extra_lore")
  var showExtraLore = true
*/

    companion object {
        lateinit var mod: LiteModDRPings

        @JvmStatic
        var latestSettings: CPacketClientSettings? = null
    }


    override fun upgradeSettings(v: String?, c: File?, o: File?) {}

    override fun onChat(
        chat: ITextComponent,
        message: String,
        newMessage: LiteLoaderEventBroker.ReturnValue<ITextComponent>?
    ): Boolean {
        val msg = chat.unformattedText
        val pingmatches = locationPing.find(msg)

        if (pingmatches != null) {
            val text = pingmatches.value;
            val split = text.split(" ")
            val len = split.size
            val x = split[len - 4].toInt()
            val y = split[len - 3].toInt()
            val z = split[len - 2].toInt()
            val name = split[len - 6]
            var frog = false
            for (thing in pings) {
                if (thing.`is`(name)) {
                    thing.x = x
                    thing.y = y
                    thing.z = z
                    frog = true
                }
            }
            if (!frog) {
                pings.add(PingThing(name, x, y, z))
            }
            /*minecraft?.ingameGUI?.addChatMessage(
                ChatType.CHAT,
                TextComponentString("Recieved Ping From "+name+" At "+ x+" "+y+" "+z+" total pings: "+ pings.size) )*/
            return false
        }
        return true
    }

    override fun getConfigPanelClass(): Class<out ConfigPanel> {
        return DRPingsPanel::class.java
    }


    override fun getHandledPackets(): MutableList<Class<out Packet<*>>> =
        mutableListOf(
            SPacketChat::class.java
        )

    private var scoreWasUpdated = mutableMapOf<String, Int>()
    private var latestCurrentHealth = mutableMapOf<String, Float>()

    private var maxHealthValues = mutableMapOf<String, Int>()

    data class UpdateInfo(var goodToUpdate: Boolean, val freshHealth: Int)

    override fun handlePacket(netHandler: INetHandler?, packet: Packet<*>?): Boolean {
        if (minecraft.player == null) return true


        return true
    }


    private fun recursiveRayTrace(world: World, start: Vec3d, end: Vec3d): BlockPos? {
        val result = world.rayTraceBlocks(start, end, false)
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            var blockPos = result.blockPos
            if (world.getBlockState(blockPos).block !== Blocks.AIR) {
                return blockPos
            }

            return blockPos
        }
        return null
    }

    private fun renderTick(pings: ArrayList<PingThing>, partialTicks: Float) {
        for (ding in pings) {
            val pos = Position(ding.x.toDouble(), ding.y.toDouble(), ding.z.toDouble())
            val pos2d = get2DCoordinates(pos.x, pos.y, pos.z)
            if (pos2d != null) {
                if (pos2d.z < 0.01 || pos2d.z > 1.0) continue


                GlStateManager.pushMatrix()

                GlStateManager.translate(pos2d.x, pos2d.y, 0.0)
                val cameraPo = cameraPos()
                // Distance scaling
                val me = minecraft.player
                val mypos = Position(
                    me.posX + (me.posX - me.lastTickPosX) * partialTicks + cameraPo.x,
                    me.posY + (me.posY - me.lastTickPosY) * partialTicks + cameraPo.y,
                    me.posZ + (me.posZ - me.lastTickPosZ) * partialTicks + cameraPo.z
                )

                var dist = dist(mypos, pos.copy(y = pos.y + 2.0))
                dist = dist.coerceIn(minDist..maxDist)
                val scaley = 20.0 / dist
//      val fovSetting = minecraft.gameSettings.fovSetting
//      val fovModifier = 1.0//;//minecraft.entityRenderer.getFOVModifier()

                val coolScale = scaley * scale
                GlStateManager.scale(coolScale, coolScale, coolScale)

                drawTag(ding.name)

                GlStateManager.popMatrix()
            }
        }
    }

    private fun drawTag(name: String) {
        val xBuffer = 4
        val yBuffer = 3
        val fontRenderer = minecraft.fontRenderer
        val strLength = fontRenderer.getStringWidth(name)

        val width = strLength + xBuffer * 2
        val height = fontRenderer.FONT_HEIGHT * 2 + yBuffer * 2

        val borderSize = 1
        val borderColor = 0xFF000000.toInt()
        val innerColor = 0x99000000.toInt()

        val x = -width / 2
        val y = -height / 2

        // Lines
//    Gui.drawRect(x, y, x + width, y + borderSize, borderColor) //top
//    Gui.drawRect(x + width - borderSize, y, x + width, y + height, borderColor) //right
//    Gui.drawRect(x, y + height - borderSize, x + width, y + height, borderColor) //botton
//    Gui.drawRect(x, y, x + borderSize, y + height, borderColor) //left

        Gui.drawRect(
            x,
            y,
            x + width,
            y + height,
            innerColor
        )
        val string = name
        fontRenderer.drawString(
            string,
            x + xBuffer,
            y + yBuffer + 1,
            0xFFFFFF
        )



    }

    private var scale =.15
    private var minDist =4.0
    private var maxDist =35.0
    private fun dist(p1: Position, p2: Position): Double {
        val xDelta: Double = p1.x - p2.x
        val yDelta: Double = p1.y - p2.y
        val zDelta: Double = p1.z - p2.z
        return sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta)
    }


    override fun onTick(
        minecraft: Minecraft?,
        partialTicks: Float,
        inGame: Boolean,
        clock: Boolean
    ) {
        if (minecraft?.player == null) {

        }


        renderTick(pings, partialTicks)
        if (inGame && minecraft?.currentScreen == null) {
            if (pingKeybind.isPressed) {

                val player = minecraft!!.player
                val world = minecraft!!.world

                val partialTicks = minecraft!!.renderPartialTicks
                val reachDistance = 200.0
                val eyePosition = player.getPositionEyes(partialTicks)
                val lookVector = player.getLook(partialTicks)

                val reachPoint = eyePosition.add(lookVector.scale(reachDistance))

                val result = recursiveRayTrace(world, eyePosition, reachPoint);
                if (result != null) {

                    val x = result.x
                    val y = result.y
                    val z = result.z





                    minecraft?.player?.sendChatMessage("/g ^ABC%D " + x + " " + y + " " + z)

                }

            }
        }


    }

    fun displayWaypoint(
        playerX: Double,
        playerY: Double,
        playerZ: Double,
        yawDegrees: Double,
        pitchDegrees: Double,
        screenWidth: Int,
        screenHeight: Int,
        waypointX: Double,
        waypointY: Double,
        waypointZ: Double,
        pointname: String
    ) {

        /*val playerPos = Vec3d(playerX, playerY, playerZ)
        val waypointPos = Vec3d(waypointX, waypointY, waypointZ)

        val transformedPos = waypointPos.subtract(playerPos).rotateYaw((-(Math.PI.toFloat() / 180.0f * yawDegrees).toDouble()).toFloat()).rotatePitch(
            (-(Math.PI.toFloat() / 180.0f * pitchDegrees).toDouble()).toFloat()
        )

        val viewDir = Vec3d(0.0, 0.0, 1.0).rotateYaw((-(Math.PI.toFloat() / 180.0f * yawDegrees).toDouble()).toFloat()).rotatePitch(
            (-(Math.PI.toFloat() / 180.0f * pitchDegrees).toDouble()).toFloat()
        )

        val dotProduct = transformedPos.dotProduct(viewDir)

        if (dotProduct < 0) {
            return
        }

        transformedPos.add(playerPos)

        val x = screenWidth / 2.0 + (transformedPos.x / transformedPos.z) * screenWidth / 2.0
        val y = screenHeight / 2.0 - (transformedPos.y / transformedPos.z) * screenHeight / 2.0*/
        /*val fov = minecraft.gameSettings.fovSetting
        val dx = playerX-waypointX
        val dy = playerZ - waypointZ
        val wfi: Double = correctAngle((atan2(dx, dy) * (180 / PI)).toFloat())
        val pfi: Double = correctAngle(pitchDegrees.toFloat() % 360)
        val a0: Double = pfi - fov / 2
        val a1: Double = pfi + fov / 2
        val ax: Double = correctAngle((2 * pfi - wfi).toFloat())
        val scale: Double = (clamp(ax, a0, a1) - a0) / fov
        // Draw the waypoint on the screen
        val x =
            round(clamp((screenWidth - screenWidth * scale  / 2).toDouble(), 0.0, screenWidth.toDouble()))
                .toInt()
        val y: Int = screenHeight/2 + 50*/

        val xy = get2DCoordinates(waypointX, waypointY, waypointZ)

        if (xy != null) {
            drawWaypoint(xy.x.toInt(), xy.y.toInt(), pointname)
        }
    }


    fun cameraPos(): Position {
        val cameraPos = BufferUtils.createFloatBuffer(16)
        val viewport = BufferUtils.createIntBuffer(16)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(
            GL11.GL_MODELVIEW_MATRIX,
            modelView
        )
        GL11.glGetFloat(
            GL11.GL_PROJECTION_MATRIX,
            projection
        )
        GL11.glGetInteger(
            GL11.GL_VIEWPORT,
            viewport
        )

        GLU.gluUnProject(
            ((viewport[2] - viewport[0]) / 2).toFloat(),
            ((viewport[3] - viewport[1]) / 2).toFloat(),
            0.0F,
            modelView,
            projection,
            viewport,
            cameraPos
        )

        return Position(
            cameraPos[0].toDouble(),
            cameraPos[1].toDouble(),
            cameraPos[2].toDouble()
        )
    }

    fun get2DCoordinates(x: Double, y: Double, z: Double): Position? {
        val screenCoordinates = BufferUtils.createFloatBuffer(3)
        val viewport = BufferUtils.createIntBuffer(16)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection)
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport)
        val result = GLU.gluProject(
            x.toFloat(),
            y.toFloat(),
            z.toFloat(),
            modelView,
            projection,
            viewport,
            screenCoordinates
        )

        return if (result) Position(
            screenCoordinates[0].toDouble(),
            Display.getHeight() - screenCoordinates[1].toDouble(),
            screenCoordinates[2].toDouble()
        ) else null
    }

    private fun correctAngle(angle: Float): Double {
        return if (angle < 0) angle + 360.0 else if (angle >= 360.0) angle - 360.0 else angle.toDouble()
    }

    fun drawWaypoint(x: Int, y: Int, text: String) {
        val minecraft = Minecraft.getMinecraft()

        // Set the render color to white
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        // Bind the texture for the label background
        minecraft.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        // Set up the texture coordinates for the label background
        val uMin = 16 / 256.0
        val vMin = 16 / 256.0
        val uMax = 32 / 256.0
        val vMax = 32 / 256.0

        // Calculate the size of the label background
        val labelWidth = minecraft.fontRenderer.getStringWidth(text) + 4
        val labelHeight = minecraft.fontRenderer.FONT_HEIGHT + 4

        // Calculate the position of the label background
        val backgroundX = x - labelWidth / 2
        val backgroundY = y - labelHeight / 2

        // Draw the label background
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        bufferBuilder.pos(backgroundX.toDouble(), (backgroundY + labelHeight).toDouble(), 0.0).tex(uMin, vMax)
            .endVertex()
        bufferBuilder.pos((backgroundX + labelWidth).toDouble(), (backgroundY + labelHeight).toDouble(), 0.0)
            .tex(uMax, vMax).endVertex()
        bufferBuilder.pos((backgroundX + labelWidth).toDouble(), backgroundY.toDouble(), 0.0).tex(uMax, vMin)
            .endVertex()
        bufferBuilder.pos(backgroundX.toDouble(), backgroundY.toDouble(), 0.0).tex(uMin, vMin).endVertex()
        tessellator.draw()

        // Draw the label text
        minecraft.fontRenderer.drawString(
            text,
            x - minecraft.fontRenderer.getStringWidth(text) / 2,
            y - minecraft.fontRenderer.FONT_HEIGHT / 2,
            -1
        )
    }

    override fun onPreRenderHUD(screenWidth: Int, screenHeight: Int) {
        renderTick(pings, minecraft.renderPartialTicks)

        // Get the player's position and orientation
        val player = Minecraft.getMinecraft().player
        val px = player.posX
        val py = player.posY + 1
        val pz = player.posZ
        val pitch = player.pitchYaw.x.toDouble()
        val yaw = player.pitchYaw.y.toDouble()

        for (waypoint in pings) {
            val xPos = waypoint.x.toDouble()
            val yPos = waypoint.y.toDouble()
            val zPos = waypoint.z.toDouble()
            val text = waypoint.name
            //renderLabel(waypoint, text, screenWidth.toDouble(), -minecraft.renderManager.viewerPosY, screenHeight.toDouble())
            //val xy = calculateScreenCoordinate(screenWidth, screenHeight, px, py, pz, xPos, yPos, zPos, pitch, yaw)
            //drawStringAtLocation(minecraft.fontRenderer, xy.first,xy.second, text)
            // Adjust the Y offset for the next waypoint
            displayWaypoint(px, py, pz, yaw, pitch, screenWidth, screenHeight, xPos, yPos, zPos, text)
        }

    }

    fun calculateScreenCoordinate(
        screenWidth: Int,
        screenHeight: Int,
        observerX: Double,
        observerY: Double,
        observerZ: Double,
        targetX: Double,
        targetY: Double,
        targetZ: Double,
        pitch: Double,
        yaw: Double
    ): Pair<Double, Double> {
        // Calculate the distance between the observer and the target
        val dx = targetX - observerX
        val dy = targetY - observerY
        val dz = targetZ - observerZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        // Calculate the angle between the observer's direction of view and the target
        val pitchAngle = atan2(dz, sqrt(dx * dx + dy * dy)) - pitch
        val yawAngle = atan2(dy, dx) - yaw

        // Calculate the xy coordinates on the screen that correspond to the target
        val screenX = screenWidth / 2.0 - distance * tan(yawAngle) * screenWidth / (2 * atan(0.5))
        val screenY = screenHeight / 2.0 - distance * tan(pitchAngle) * screenHeight / (2 * atan(0.5))

        return Pair(screenX, screenY)
    }

    fun drawStringAtLocation(fontRenderer: FontRenderer, x: Double, y: Double, text: String) {
        minecraft.fontRenderer.drawStringWithShadow(text, x.toFloat(), y.toFloat(), 1)
    }

    fun distanceBetweenPoints(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        return Math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    protected fun renderLabel(
        par1EntityWaypoint: PingThing,
        par2Str: String?,
        par3: Double,
        par5: Double,
        par7: Double
    ) {
        var par2Str = par2Str
        var par3 = par3
        var par5 = par5
        var par7 = par7
        val px = minecraft.player.posX
        val py = minecraft.player.posX + minecraft.player.eyeHeight
        val pz = minecraft.player.posZ
        val wx = par1EntityWaypoint.x.toDouble()
        val wy = par1EntityWaypoint.y.toDouble() + 1
        val wz = par1EntityWaypoint.z.toDouble()
        val dist = distanceBetweenPoints(px, py, pz, wz, wy, wz)
        var var10 = Math.sqrt(dist)
        if (var10 <= 1000) {
            par2Str += " (" + var10.toInt() + "m)"

            minecraft.renderManager
            val var12: FontRenderer = minecraft.fontRenderer
            //float var13 = 1.6F; // the usual label rendering size
            //float var14 = 0.016666668F * var13;
            val var14 = (var10.toFloat() * 0.1f + 1.0f) * 0.0266f //lower first higher second exaggerates the difference
            GL11.glPushMatrix()
            GL11.glTranslatef(par3.toFloat() + 0.5f, par5.toFloat() + 1.3f, par7.toFloat() + 0.5f)
            GL11.glNormal3f(0.0f, 1.0f, 0.0f)
            GL11.glRotatef(-minecraft.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            GL11.glRotatef(minecraft.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
            GL11.glScalef(-var14, -var14, var14)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_FOG)
            GL11.glDepthMask(false)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            val var16: Byte = 0
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            val var17 = var12.getStringWidth(par2Str) / 2


            GL11.glEnable(GL11.GL_TEXTURE_2D)
            var12.drawString(
                par2Str,
                -var12.getStringWidth(par2Str) / 2,
                var16.toInt(),
                0xffaaaaaa.toInt()
            ); // draw grey with no depth then white with depth.  White shows if it's in front, grey otherwise
            //GL11.glEnable(GL11.GL_DEPTH_TEST); // except we comment out the grey, and just draw the white in front of everything
            //GL11.glDepthMask(true);
            var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16.toInt(), -1)

            GL11.glPopMatrix()
        }
    }


    fun getScreenCoords(mc: Minecraft, player: EntityPlayerSP, x: Double, y: Double, z: Double): Pair<Int, Int>? {
        val dx = x - player.posX
        val dy = y - player.posY
        val dz = z - player.posZ
        val yaw = Math.toRadians(player.rotationYaw.toDouble())
        val pitch = Math.toRadians(player.rotationPitch.toDouble())
        val cosYaw = Math.cos(yaw)
        val sinYaw = Math.sin(yaw)
        val cosPitch = Math.cos(pitch)
        val sinPitch = Math.sin(pitch)
        val relX = dy * sinYaw - dz * cosYaw
        val relY = dx * sinPitch + dy * cosYaw * cosPitch + dz * sinYaw * cosPitch
        val relZ = dx * cosPitch - dy * cosYaw * sinPitch + dz * sinYaw * sinPitch
        val projX = mc.displayWidth / 2.0 + relX * mc.displayHeight / 2.0 / player.distanceWalkedOnStepModified
        val projY = mc.displayHeight / 2.0 - relY * mc.displayHeight / 2.0 / player.distanceWalkedOnStepModified
        if (relZ < 0.0) return null
        return Pair(projX.toInt(), projY.toInt())
    }

    override fun onPostRenderHUD(screenWidth: Int, screenHeight: Int) {


    }

    override fun getName(): String = "DR Ping Mod"
    override fun getVersion(): String = "1.1"

    @Throws(ReflectiveOperationException::class)
    private fun createDefaultEnvironment(): ControllerEnvironment? {
        val constructor: Constructor<ControllerEnvironment> =
            Class.forName("net.java.games.input.DefaultControllerEnvironment")
                .declaredConstructors[0] as Constructor<ControllerEnvironment>
        constructor.isAccessible = true

        return constructor.newInstance()
    }


    override fun init(configPath: File?) {
        mod = this

        LiteLoader.getInput().registerKeyBinding(pingKeybind)


    }
}

data class Position(var x: Double, var y: Double, var z: Double)