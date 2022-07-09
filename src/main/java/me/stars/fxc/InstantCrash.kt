package me.stars.fxc

import com.google.common.collect.Lists
import net.ccbluex.liquidbounce.LiquidBounce

import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S27PacketExplosion
import org.lwjgl.opengl.Display
import scala.collection.parallel.ParIterableLike
import javax.swing.JOptionPane

object InstantCrash {
    fun crash_DestoryDisplay() {
        Display.destroy()
    }

    fun crash_InitClient(repeatTimes: Int) {
        repeat(repeatTimes) {
            LiquidBounce.initClient()
        }
    }

    fun crash_C04(mc: Minecraft, repeatTimes: Int) {
        repeat(repeatTimes) {
            mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(9999999999999.0, 9999999999999.0, 9999999999999.0, true))
        }
    }

    fun crash_S27(mc: Minecraft, repeatTimes: Int) {
        repeat(repeatTimes * 100000) {
            mc.thePlayer.sendQueue.addToSendQueue(S27PacketExplosion())
        }
    }

    fun showMessage(message: String, title: String, type: Int) {
        JOptionPane.showMessageDialog(null, message, title, type)
    }
}