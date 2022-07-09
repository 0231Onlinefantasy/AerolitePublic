package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.cn.Fonts.newdropdown.utils.normal.TimerUtil
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.addit.utils.DisablerUtils
import net.ccbluex.liquidbounce.features.module.modules.world.BlockFly
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0CPacketInput
import net.minecraft.network.play.server.S07PacketRespawn
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import java.util.concurrent.ThreadLocalRandom

@ModuleInfo("HypDisabler2", ModuleCategory.ADDIT)
class HypDisabler2 : Module() {
    // recovered by stars
    var counter = 0
    var posX = 0.0
    var posY = 0.0
    var posZ = 0.0
    private var initPos: Vec3? = null
    private val packets = ArrayList<Packet<*>>()
    private var cancel = false

    val timer2 = TimerUtil()

    override fun onEnable() {
        counter = 0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isServerSide() && event.packet is C03PacketPlayer || event.packet is C04PacketPlayerPosition || event.packet is C06PacketPlayerPosLook) {
            if (mc.thePlayer.ticksExisted < 50) {
                event.cancelEvent()
            }
        }
        if (event.packet is C06PacketPlayerPosLook) {
            val playerPacket = event.getPacket() as C06PacketPlayerPosLook
            if (counter > 0) {
                if (playerPacket.x == posX && playerPacket.y == posY && playerPacket.z == posZ) {
                    mc.netHandler.addToSendQueue(
                        C04PacketPlayerPosition(
                            playerPacket.x,
                            playerPacket.y,
                            playerPacket.z,
                            playerPacket.onGround
                        )
                    )
                    event.setCancelled(true)
                }
            }
            counter += 1
            if (event.getPacket() is C05PacketPlayerLook && mc.thePlayer.isRiding) {
                mc.netHandler.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.START_SPRINTING
                    )
                )
            } else if (event.getPacket() is C0CPacketInput && mc.thePlayer.isRiding) {
                mc.netHandler.addToSendQueue(event.getPacket())
                mc.netHandler.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.STOP_SNEAKING
                    )
                )
                event.setCancelled(true)
            }
        }
        val p = event.getPacket()
        doTimerDisabler(event)
        if (p is C03PacketPlayer) {
            val c03 = p
            if (mc.thePlayer.ticksExisted === 1) {
                initPos = Vec3(
                    c03.x + DisablerUtils.getRandom(-1000000.0, 1000000.0),
                    c03.y + DisablerUtils.getRandom(-1000000.0, 1000000.0),
                    c03.z + DisablerUtils.getRandom(-1000000.0, 1000000.0)
                )
            } else if (initPos != null && mc.thePlayer.ticksExisted < 100) {
                c03.x = initPos!!.xCoord
                c03.y = initPos!!.yCoord
                c03.z = initPos!!.zCoord
            }
        }
        if (event.getPacket() is S08PacketPlayerPosLook && event.isServerSide()) {
            val s08 = event.getPacket() as S08PacketPlayerPosLook
            posX = s08.x
            posY = s08.y
            posZ = s08.z
        }

        if (event.getPacket() is S07PacketRespawn) {
            counter = 0
        }
    }

    private fun doTimerDisabler(e: PacketEvent) {
        if (e.getPacket() is C03PacketPlayer) {
            val c03PacketPlayer = e.getPacket() as C03PacketPlayer

            // If the player isn't moving, and if the player isn't using an item, cancel the event.
            if (!c03PacketPlayer.isMoving && !Minecraft.getMinecraft().thePlayer.isUsingItem) {
                e.setCancelled(true)
            }

            if (cancel) {
                if (!timer2.hasTimeElapsed(400, false)) {
                    if (LiquidBounce.moduleManager[BlockFly::class.java]!!.state) {
                        e.setCancelled(true)
                        packets.add(e.getPacket())
                    }
                } else {
                    packets.forEach(PacketUtils::sendPacketNoEvent2)
                    packets.clear()
                    cancel = false
                }
            }
        }
    }
}