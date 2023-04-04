package com.beanz.drpings

import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.core.LiteLoaderEventBroker
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import net.java.games.input.ControllerEnvironment
import net.minecraft.client.Minecraft
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
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import java.io.File
import java.lang.reflect.Constructor


val minecraft: Minecraft
    get() = Minecraft.getMinecraft()


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
            val x = split[len-4]
            val y = split[len-3]
            val z = split[len-2]
            val name = split[len-6]
            minecraft?.ingameGUI?.addChatMessage(
                ChatType.CHAT,
                TextComponentString("Recieved Ping From "+name+" At "+ x+" "+y+" "+z) )
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

    override fun onPreRenderHUD(screenWidth: Int, screenHeight: Int) {

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
