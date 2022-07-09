package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.addit.utils.Angle
import net.ccbluex.liquidbounce.features.module.modules.addit.utils.AngleUtility
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.Strafe
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.MathHelper
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

@ModuleInfo(name = "HypDisabler", ModuleCategory.ADDIT)
class HypDisabler : Module() {
    private val c0f = BoolValue("C0F ID Edit", false)
    private val s08 = BoolValue("Cancel S08", true)
    private val c04 = BoolValue("C03 When S08", false).displayable { s08.get() }
    var confirmTransactionQueue: Queue<C0FPacketConfirmTransaction> = ConcurrentLinkedQueue()
    var keepAliveQueue: Queue<C00PacketKeepAlive> = ConcurrentLinkedQueue()
    var clickWindowPackets = CopyOnWriteArrayList<C0EPacketClickWindow>()
    var disabled = false
    var lastuid = 0
    var lastRelease = MSTimer()
    var cancelledPackets = 0
    var angleUtility = AngleUtility(110f, 120f, 30f, 40f)
    var lastAngle: Angle? = null
    var yawDiff = 0f

    @EventTarget
    fun onWorld(event: WorldEvent) {
        confirmTransactionQueue.clear()
        keepAliveQueue.clear()
        disabled = false
        clickWindowPackets.clear()
        lastuid = 0
        cancelledPackets = 0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val p = event.packet
        var x = 0.0
        var y = 0.0
        var z = 0.0
        if (disabled) {
            if (p is C03PacketPlayer && !(p is C04PacketPlayerPosition || p is C05PacketPlayerLook || p is C06PacketPlayerPosLook)) {
                cancelledPackets++
                event.cancelEvent()
            }
        }
        if (c0f.get() && p is C0FPacketConfirmTransaction) {
            if (p.windowId == 0 && p.uid < 0 && p.uid != (-1).toShort()) {
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    event.cancelEvent()
                }
            }
        }

        if (s08.get() && p is S08PacketPlayerPosLook && event.isServerSide()) {
            x = p.x
            y = p.y
            z = p.z
            ClientUtils.displayChatMessage("[HypDisabler] S08 Receive: $x $y $z")
            if (!disabled && mc.thePlayer.ticksExisted > 20) {
                event.cancelEvent()
                if (c04.get()) mc.netHandler.networkManager.sendPacket(C04PacketPlayerPosition(x, y, z, mc.thePlayer.onGround))
            }
        }
        if (event.packet is C06PacketPlayerPosLook) {
            if (event.packet.x == x && event.packet.y == y && event.packet.z == z) {
                mc.netHandler.networkManager.sendPacket(C04PacketPlayerPosition(event.packet.x, event.packet.y, event.packet.z, event.packet.onGround))
                ClientUtils.displayChatMessage("[HypDisabler] C04 Sent")
                event.cancelEvent()
            }
            if (event.packet is C05PacketPlayerLook && mc.thePlayer.isRiding) {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
            } else if (event.packet is C0CPacketInput && mc.thePlayer.isRiding) {
                mc.netHandler.networkManager.sendPacket(event.packet)
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
                event.cancelEvent()
            }
        }

        if (p is C0FPacketConfirmTransaction) {
            processConfirmTransactionPacket(event)
        } else if (p is C00PacketKeepAlive) {
            processKeepAlivePacket(event)
        } else if (p is C03PacketPlayer) {
            processPlayerPosLooksPacket(event)
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.ticksExisted % 40 == 0) {
            val rate = (cancelledPackets / 40f * 100).toInt()
            ClientUtils.displayChatMessage("[HypDisabler] Movement Handler: $rate%")
            cancelledPackets = 0
        }
        val m = LiquidBounce.moduleManager
        if (m[Strafe::class.java]!!.state || m[Speed::class.java]!!.state) {
            var targetYaw= event.yaw
            if (mc.gameSettings.keyBindBack.pressed) {
                targetYaw += 180f
                if (mc.gameSettings.keyBindLeft.pressed) {
                    targetYaw += 45f
                }
                if (mc.gameSettings.keyBindRight.pressed) {
                    targetYaw -= 45f
                }
            } else if (mc.gameSettings.keyBindForward.pressed) {
                if (mc.gameSettings.keyBindLeft.pressed) {
                    targetYaw -= 45f
                }
                if (mc.gameSettings.keyBindRight.pressed) {
                    targetYaw += 45f
                }
            } else {
                if (mc.gameSettings.keyBindLeft.pressed) {
                    targetYaw -= 90f
                }
                if (mc.gameSettings.keyBindRight.pressed) {
                    targetYaw += 90f
                }
            }
            val angle = angleUtility.smoothAngle(
                Angle(targetYaw, event.yaw),
                lastAngle,
                120f,
                360f)
            yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - angle.getYaw())
            event.yaw = angle.yaw
        }
        lastAngle = Angle(event.yaw, event.pitch)

        if (disabled) {
            if (confirmTransactionQueue.isEmpty()) {
                lastRelease.reset()
            } else {
                if (confirmTransactionQueue.size >= 6) {
                    while (!keepAliveQueue.isEmpty()) PacketUtils.sendPacketNoEvent(keepAliveQueue.poll())
                    while (!confirmTransactionQueue.isEmpty()) {
                        val poll = confirmTransactionQueue.poll()
                        PacketUtils.sendPacketNoEvent(poll)
                    }
                }
            }
        }
    }

    private fun processConfirmTransactionPacket(e: PacketEvent) {
        val packet = e.packet as C0FPacketConfirmTransaction
        val windowId = packet.windowId
        val uid = packet.uid.toInt()
        if (windowId != 0 || uid >= 0) {
            ClientUtils.displayChatMessage("[HypDisabler] Inventory C0F Released")
        } else {
            if (uid == --lastuid) {
                if (!disabled) {
                    ClientUtils.displayChatMessage("[HypDisabler] Watchdog Disabled!")
                    disabled = true
                }
                confirmTransactionQueue.offer(packet)
                e.cancelEvent()
            }
            lastuid = uid
        }
    }

    private fun processKeepAlivePacket(e: PacketEvent) {
        val packet = e.packet as C00PacketKeepAlive
        if (disabled) {
            keepAliveQueue.offer(packet)
            e.cancelEvent()
        }
    }


    private fun processPlayerPosLooksPacket(e: PacketEvent) {
        if (!disabled && !ServerUtils.isHypixelLobby()) {
            e.cancelEvent()
        }
    }
}

