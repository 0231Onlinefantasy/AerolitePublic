package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*

@ModuleInfo(name = "PacketFixer", category = ModuleCategory.ADDIT)
class PacketFixer : Module() {

    // settings
    private val fixBlinkAndFreecam = BoolValue("Fix3YBadPackets_BlinkFreecam", true)
    private val fixPacketPlayer = BoolValue("Fix3ABadPackets_C03s", true)
    private val fixItemSwap = BoolValue("Fix14DScaffold_ItemSwap", true)
    private val fixGround = BoolValue("Fix4IFly_Ground", true)
    private val fixIdleFly = BoolValue("Fix4CFly_Idle", false)

    // local variables
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var yaw = 0.0F
    private var pitch = 0.0F
    private var jam = 0
    private var packetCount = 0
    private var prevSlot = -1

    // events
    override fun onEnable() {
        jam = 0
        packetCount = 0
        prevSlot = -1

        if (mc.thePlayer == null) return
        x = mc.thePlayer.posX
        y = mc.thePlayer.posY
        z = mc.thePlayer.posZ
        yaw = mc.thePlayer.rotationYaw
        pitch = mc.thePlayer.rotationPitch
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return

        val packet = event.packet

        // fix ground check (4I)
        if (fixGround.get() && packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) {
            if ((mc.thePlayer.motionY == 0.0 || (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically)) && !packet.onGround)
                packet.onGround = true
        }

        // some info things
        if (packet is C04PacketPlayerPosition) {
            x = packet.x
            y = packet.y
            z = packet.z
            jam = 0
        }

        if (packet is C05PacketPlayerLook) {
            yaw = packet.yaw
            pitch = packet.pitch
        }

        if (packet is C06PacketPlayerPosLook) {
            x = packet.x
            y = packet.y
            z = packet.z
            jam = 0

            yaw = packet.yaw
            pitch = packet.pitch
        }

        // fix bad packets, caused by timer or fast use
        if (fixPacketPlayer.get() && packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) {
            jam++
            if (jam > 20) {
                jam = 0
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(x, y, z, yaw, pitch, packet.onGround))
            }
        }

        // fix scaffold duplicated hotbar switch
        if (fixItemSwap.get() && packet is C09PacketHeldItemChange) {
            if (packet.getSlotId() == prevSlot) {
                event.cancelEvent()
            } else {
                prevSlot = packet.getSlotId()
            }
        }

        // fix blink and freecam cancelling c03s while sending c00
        if (fixBlinkAndFreecam.get() && (LiquidBounce.moduleManager.getModule(Blink::class.java)!!.state || LiquidBounce.moduleManager.getModule(FreeCam::class.java)!!.state) && packet is C00PacketKeepAlive)
            event.cancelEvent()

        // fix fly while not moving, reduce some checks (4C)
        if (fixIdleFly.get() && packet is C03PacketPlayer && !packet.onGround) {
            if (packet !is C04PacketPlayerPosition && packet !is C05PacketPlayerLook && packet !is C06PacketPlayerPosLook) {
                packetCount++
                if (packetCount >= 2)
                    event.cancelEvent()
            } else {
                packetCount = 0
            }
        }
    }

}

