package com.beanz.drpings

import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.core.LiteLoaderEventBroker
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import net.java.games.input.ControllerEnvironment
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketClientSettings
import net.minecraft.network.play.server.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import org.lwjgl.util.Color
import java.io.File
import java.lang.Math.*
import java.lang.reflect.Constructor
import kotlin.math.abs


val minecraft: Minecraft
    get() = Minecraft.getMinecraft()

var pings =  ArrayList<PingThing>()
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
            val x = split[len-4].toInt()
            val y = split[len-3].toInt()
            val z = split[len-2].toInt()
            val name = split[len-6]
            var frog = false
            for (thing in pings){
                if (thing.`is`(name)){
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
    override fun onTick(
        minecraft: Minecraft?,
        partialTicks: Float,
        inGame: Boolean,
        clock: Boolean
    ) {
        if (minecraft?.player == null) {

        }

        if (inGame && minecraft?.currentScreen == null) {
            if (pingKeybind.isPressed) {

                val player = minecraft!!.player
                val world= minecraft!!.world

                val partialTicks = minecraft!!.renderPartialTicks
                val reachDistance = 200.0
                val eyePosition= player.getPositionEyes(partialTicks)
                val lookVector = player.getLook(partialTicks)

                val reachPoint = eyePosition.add(lookVector.scale(reachDistance))

                val  result = recursiveRayTrace(world, eyePosition, reachPoint);
                if (result!= null) {

                    val x = result.x
                    val y = result.y
                    val z = result.z





                    minecraft?.player?.sendChatMessage("/g ^ABC%D "+x+" "+ y+" "+z)

                }

            }
        }



    }

    fun displayWaypoint(playerX: Double, playerY: Double, playerZ: Double, yawDegrees: Double, pitchDegrees: Double, screenWidth: Int, screenHeight: Int, waypointX: Double, waypointY: Double, waypointZ: Double, pointname: String) {

        val dx = waypointX - playerX
        val dy = waypointY - playerY
        val dz = waypointZ - playerZ

        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        //var horizontalAngle = atan2(-dx, dz) - ((yawDegrees) * PI / 180.0)

        val horizontalAngle = atan2(-dx, dz)

        // Convert yaw to the range [-180, 180]
        val yaw = yawDegrees % 360.0
        val yawAdjusted = if (yaw > 180.0) yaw - 360.0 else yaw

        // Adjust horizontalAngle based on the player's yaw
        val horizontalAngleAdjusted = horizontalAngle - (yawAdjusted * PI / 180.0)


        val verticalAngle = atan2(dy, sqrt(dx * dx + dz * dz))

        if (verticalAngle > PI /2 || verticalAngle < -PI/2) {
            // Waypoint is behind the player, don't display it
            return
        }

        val maxHorizontalAngle = PI / 1.25

        if (abs(horizontalAngleAdjusted) > maxHorizontalAngle) {
            // Waypoint is outside the player's field of view, don't display it
            return
        }

        val halfWidth = screenWidth / 2
        val halfHeight = screenHeight / 2
        val x = halfWidth + (halfWidth * horizontalAngleAdjusted / PI)
        val y = halfHeight - (halfHeight * verticalAngle / (PI / 2))


        var extra = " "
        extra +=distance.toInt()

        // Draw the waypoint on the screen
        // Example code:
        drawWaypoint(x.toInt(), y.toInt(), pointname+extra)
    }

    fun drawWaypoint(x: Int, y: Int, text: String) {
        // Example code:
        drawStringAtLocation(minecraft.fontRenderer, x.toDouble(),y.toDouble(), text)
    }

    override fun onPreRenderHUD(screenWidth: Int, screenHeight: Int) {


        // Get the player's position and orientation
        val player = Minecraft.getMinecraft().player
        val px = player.posX
        val py = player.posY+ 1
        val pz = player.posZ
        val pitch = player.pitchYaw.x.toDouble()
        val yaw = player.pitchYaw.y.toDouble()

        for (waypoint in pings) {
            val xPos = waypoint.x.toDouble()
            val yPos = waypoint.y.toDouble()+1
            val zPos = waypoint.z.toDouble()
            val text = waypoint.name
            //renderLabel(waypoint, text, screenWidth.toDouble(), -minecraft.renderManager.viewerPosY, screenHeight.toDouble())
            //val xy = calculateScreenCoordinate(screenWidth, screenHeight, px, py, pz, xPos, yPos, zPos, pitch, yaw)
            //drawStringAtLocation(minecraft.fontRenderer, xy.first,xy.second, text)
            // Adjust the Y offset for the next waypoint
            displayWaypoint(px,py,pz,yaw,pitch,screenWidth,screenHeight,xPos,yPos,zPos,text)
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
        val py = minecraft.player.posX+ minecraft.player.eyeHeight
        val pz = minecraft.player.posZ
        val wx = par1EntityWaypoint.x.toDouble()
        val wy = par1EntityWaypoint.y.toDouble()+1
        val wz = par1EntityWaypoint.z.toDouble()
        val dist = distanceBetweenPoints(px,py,pz,wz,wy,wz)
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
            var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16.toInt(), 0xffaaaaaa.toInt()); // draw grey with no depth then white with depth.  White shows if it's in front, grey otherwise
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
