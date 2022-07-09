package net.ccbluex.liquidbounce.slib

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager.mc
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.client.Minecraft
import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.util.IChatComponent
import org.apache.logging.log4j.LogManager


object ETF {

    // Logger
    private val log = LogManager.getLogger("AeroLite")

    fun LogInfo(info: String) {
        log.info(info)
    }

    fun LogError(error: String) {
        log.error(error)
    }

    fun LogWarn(warn: String) {
        log.warn(warn)
    }

    fun LogError2(warn: String, throwable: Throwable) {
        log.error(warn, throwable)
    }

    fun LogDebug(debug: String) {
        log.debug(debug)
    }


    // Packets

    fun sendCrit1(xAdd: Double = 0.0, yAdd: Double = 0.0, zAdd: Double = 0.0, ground: Boolean) {
        val x = mc.thePlayer.posX + xAdd
        val y = mc.thePlayer.posY + yAdd
        val z = mc.thePlayer.posZ + zAdd
        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, ground))
    }

    fun sendCrit2(xAdd: Double = 0.0, yAdd: Double = 0.0, zAdd: Double = 0.0,
        pyaw: Float = 0.0F, ppitch: Float = 0.0F, ground: Boolean) {
        val x = mc.thePlayer.posX + xAdd
        val y = mc.thePlayer.posY + yAdd
        val z = mc.thePlayer.posZ + zAdd
        val yaw = mc.thePlayer.rotationYaw + pyaw
        val pitch = mc.thePlayer.rotationPitch + ppitch
        mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, yaw, pitch, ground))
    }

    fun netKeepAlive(amount: Int) {
        mc.netHandler.addToSendQueue(C00PacketKeepAlive(amount))
    }

    fun playerKeepAlive(amount: Int) {
        mc.thePlayer.sendQueue.addToSendQueue(C00PacketKeepAlive(amount))
    }

    fun netC03(avoid: Boolean) {
        mc.netHandler.addToSendQueue(C03PacketPlayer(avoid))
    }

    fun playerC03(avoid: Boolean) {
        mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer(avoid))
    }


    // Messages
    fun alert(msg: String) {
        ClientUtils.displayChatMessage("§8[" + LiquidBounce.COLORED_NAME + "§8] §f" + msg)
    }
}

