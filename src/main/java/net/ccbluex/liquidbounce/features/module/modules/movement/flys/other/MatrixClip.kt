package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sin
import kotlin.math.cos


class MatrixClip : FlyMode("MatrixClip") {
    private val clipMode = ListValue("BypassMode", arrayOf("Clip1","Clip2","Clip3"), "Clip2")
    private val yclip = FloatValue("${valuePrefix}YClip", 10f, 5f, 20f)
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()
    private val timer = MSTimer()
    private val timer2 = MSTimer()

    private var blinkTime = 0
    private var clipTime = 0
    private var disableLogger = false

    override fun onEnable() {
        timer.reset()
        timer2.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        when (clipMode.get().lowercase()) {
            "clip1" -> {
                blinkTime = 736
                clipTime = 909
            }
            "clip2" -> {
                blinkTime = 1000
                clipTime = 909
            }
            "clip3" -> {
                blinkTime = 909
                clipTime = 1000
            }
        }
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if(timer.hasTimePassed(blinkTime.toLong())) {
            timer.reset()
            try {
                disableLogger = true
                while (!packets.isEmpty()) {
                    mc.netHandler.addToSendQueue(packets.take())
                }
                disableLogger = false
            } finally {
                disableLogger = false
            }
        }
        if(timer2.hasTimePassed((clipTime.toLong()))) {
            timer2.reset()
            mc.thePlayer.setPosition(mc.thePlayer.posX , mc.thePlayer.posY + yclip.get(), mc.thePlayer.posZ)
        }
    }

    override fun onDisable() {
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || disableLogger) return
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if (packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook ||
            packet is C08PacketPlayerBlockPlacement ||
            packet is C0APacketAnimation ||
            packet is C0BPacketEntityAction || packet is C02PacketUseEntity
        ) {
            event.cancelEvent()
            packets.add(packet as Packet<INetHandlerPlayServer>)
        }
    }
}