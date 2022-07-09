package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2BPacketChangeGameState
import java.util.concurrent.ConcurrentLinkedQueue


/*
  By Stars
  Made in Aerolite
 */


@ModuleInfo(name = "ACLover", category = ModuleCategory.ADDIT)
class ACLover : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Basic", "AAC1", "AAC3Exploit", "AAC5Normal", "AAC5Scaffold", "AAC5Move", "NCP", "MoonAC-Punishments"), "Basic")
    private val aac3C0FUpValue = IntegerValue("AAC3C0F+", 0 ,0,999).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3C0FDownValue = IntegerValue("AAC3C0F-", 0 ,0,999).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3xValue = FloatValue("AAC3X", 0.99F,0.0F,0.99F).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3yValue = FloatValue("AAC3Y", 0.99F,0.0F,0.99F).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3zValue = FloatValue("AAC3Z", 0.99F,0.0F,0.99F).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3C03 = BoolValue("AAC3C03", true).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3onGround = BoolValue("AAC3OnGround", false).displayable { modeValue.equals("AAC3Exploit") }
    private val aac3rotating = BoolValue("AAC3Rotating", false).displayable { modeValue.equals("AAC3Exploit") }
    private val debugValue = BoolValue("Debug", true)

    var queueID = ConcurrentLinkedQueue<Any?>()
    var uid = -1
    var ncpEETicks = MSTimer()

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient == null || mc.theWorld == null) return
        queueID.clear()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        var packet = event.packet
        if (mc.thePlayer == null || mc.theWorld == null) return

        when (modeValue.get().lowercase()) {
            "moonac-punishments" -> {
                if (packet is S2BPacketChangeGameState && ((packet.getGameState() == 5 && !mc.isDemo()) || packet.getGameState() == 10)) {
                    // 5 = demo screen, 10 = guardian sound + animations
                    event.cancelEvent()
                    debugMessage("found attempt to trigger ${if (packet.getGameState() == 5) "demo screen" else "guardian effects"}.")
                }

                if (packet is S02PacketChat && packet.getChatComponent() != null &&
                    packet.getChatComponent().getUnformattedText().startsWith("GET TROLL", true))
                    event.cancelEvent()
            }
        }
        if (packet is C08PacketPlayerBlockPlacement) {
            when (modeValue.get().lowercase()) {
                "aac3exploit" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer())
                    debugMessage("Add C03")
                    mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction())
                    debugMessage("Add C0F")
                }
                "aac5scaffold" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction())
                    debugMessage("Add C0F")
                    mc.thePlayer.sendQueue.addToSendQueue(C0BPacketEntityAction())
                    debugMessage("Add C0B")
                    mc.thePlayer.sendQueue.addToSendQueue(C07PacketPlayerDigging())
                    debugMessage("Add C07")
                }
            }
        }
        if (packet is C0FPacketConfirmTransaction) {
            when (modeValue.get().lowercase()) {
                "aac3exploit" -> {
                    if (packet.windowId < aac3C0FUpValue.get() && packet.windowId == aac3C0FDownValue.get()) {
                        event.cancelEvent()
                        if (queueID.isEmpty())
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, uid.toShort(), false))
                        else
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, queueID.poll().toString().toShort(), false))
                        queueID.offer(packet.uid)
                        debugMessage("Sent Poll")
                    }
                }
                "aac5scaffold" -> {
                    if (packet.windowId < 10 && packet.windowId == 20) {
                        event.cancelEvent()
                        if (queueID.isEmpty())
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, uid.toShort(), false))
                        else
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, queueID.poll().toString().toShort(), false))
                        queueID.offer(packet.uid)
                        debugMessage("Sent Poll")
                    }
                }
                "aac5normal" -> {
                    if (packet.windowId < 10 && packet.windowId == 20) {
                        event.cancelEvent()
                        if (queueID.isEmpty())
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, uid.toShort(), false))
                        else
                            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(0, queueID.poll().toString().toShort(), false))
                        queueID.offer(packet.uid)
                        debugMessage("Sent Poll")
                    }
                }
            }
        }
        if (packet is C02PacketUseEntity) {
            when (modeValue.get().lowercase()) {
                "aac3exploit" -> mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity())
                "aac5normal" -> mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity())
                "aac5scaffold" -> mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity())
                "ncp" -> mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity())
            }
        }
        if (packet is C03PacketPlayer) {
            when (modeValue.get().lowercase()) {
                "aac1" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C0CPacketInput())
                    packet.y += 7.0E-9
                    debugMessage("Modified C0C")
                }
                "aac3exploit" -> {
                    if (aac3C03.get()) {
                        mc.thePlayer.sendQueue.addToSendQueue(C07PacketPlayerDigging())
                        mc.thePlayer.sendQueue.addToSendQueue(C0BPacketEntityAction())
                    }
                    packet.x += aac3yValue.get()
                    packet.y += aac3xValue.get()
                    packet.z += aac3zValue.get()
                    packet.onGround = aac3onGround.get()
                    packet.rotating = aac3rotating.get()
                    debugMessage("Modified C03")
                }
            }
        }
    }

    @EventTarget
    override fun onEnable() {
        if (modeValue.equals("NCP")) {
            if (ncpEETicks.hasTimePassed(1000)) {
                mc.timer.timerSpeed = 0.09F
            }
            if (ncpEETicks.hasTimePassed(1200)) {
                mc.timer.timerSpeed = 0.68F
            }
            if (ncpEETicks.hasTimePassed(1400)) {
                mc.timer.timerSpeed = 2.2F
            }
            if (ncpEETicks.hasTimePassed(1600)) {
                mc.timer.timerSpeed = 1.0F
                mc.thePlayer.sendQueue.addToSendQueue(C0BPacketEntityAction())
            }
            ncpEETicks.reset()
        }
    }

    @EventTarget
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0F
        ncpEETicks.reset()
    }

    private fun debugMessage(str: String) {
        if (debugValue.get()) {
            alert(" [Disabler] $str")
        }
    }
}