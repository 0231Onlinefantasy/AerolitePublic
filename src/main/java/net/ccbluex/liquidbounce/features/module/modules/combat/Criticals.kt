/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.stats.StatList

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue("Mode", arrayOf(
        "Packet", "NewPacket", "LitePacket", "FixPacket", "MiPacket", "AAC5Packet", "AAC4Packet", "HPacket",
        "NCP", "NCP2", "Vanilla", "Vulcan",
        "Kawaii", "NonCalc", "Invaild", "LowPhase",
        "Edit", "Edit2", "Hypixel", "HypixelTest",
        "AACNoGround", "NoGround", "Redesky",
        "VerusSmart", "MatrixSmart", "Blocksmc", "Minemora",
        "Motion", "Hover", "Custom"),
        "packet")
    // Other Lists
    private val motionValue = ListValue("MotionMode", arrayOf("RedeSkyLowHop", "Hop", "Jump", "LowJump", "MinemoraTest", "TPHop", "AAC5", "NCPSilent"), "Jump")
    private val hoverValue = ListValue("HoverMode", arrayOf("AAC4", "AAC4Other", "OldRedesky", "Normal1", "Normal2", "Normal3","Minis", "Minis2", "TPCollide", "2b2t"), "AAC4")
    // Hover
    private val hoverNoFall = BoolValue("HoverNoFall", true).displayable { modeValue.equals("Hover") }
    private val hoverCombat = BoolValue("HoverOnlyCombat", true).displayable { modeValue.equals("Hover") }
    // Bypass
    private val delayValue = IntegerValue("Delay", 0, 0, 1000)
    private val s08FlagValue = BoolValue("FlagPause", true)
    private val s08DelayValue = IntegerValue("FlagPauseTime", 100, 0, 5000).displayable { s08FlagValue.get() }
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 20)
    private val lookValue = BoolValue("UseC06Packet", false)
    // Debug
    private val debugValue = ListValue("Debug", arrayOf("Off","ChatA","ChatB","ChatC","ChatD","NotiA","NotiB","NotiC","NotiD"), "Off")
    // NoGround
    private val rsNofallValue = BoolValue("NofallHelper",true).displayable { modeValue.equals("AACNoGround") }
    private val badGroundValue = BoolValue("BadGround", false).displayable { modeValue.equals("NoGround") || modeValue.equals("AACNoGround") }
    // Other
    private val attackTimesValue = IntegerValue("AttackTimes", 0, 5, 10).displayable { modeValue.equals("VerusSmart") || modeValue.equals("Vulcan") || modeValue.equals("TestPacket") }
    private val resetMotionValue = BoolValue("ResetMotion", false)

    val msTimer = MSTimer()

    val flagTimer = MSTimer()

    // Critical Checks
    private var canCrits = true
    private var readyCrits = false
    private var counter = 0

    private var target = 0
    var jState = 0
    var aacLastState = false

    override fun onEnable() {
        if (modeValue.equals("NoGround") && !badGroundValue.get()) {
            mc.thePlayer.jump()
        }
        if (resetMotionValue.get()) {
            mc.thePlayer.motionX *= 0
            mc.thePlayer.motionZ *= 0
        }
        jState = 0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        if (resetMotionValue.get()) {
            mc.thePlayer.motionX *= 0
            mc.thePlayer.motionZ *= 0
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                !msTimer.hasTimePassed(delayValue.get().toLong())) {
                return
            }

            if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
                return



            fun sendCriticalPacket(xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0, ground: Boolean) {
                val x = mc.thePlayer.posX + xOffset
                val y = mc.thePlayer.posY + yOffset
                val z = mc.thePlayer.posZ + zOffset
                if (lookValue.get()) {
                    mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, ground))
                } else {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, ground))
                }
            }

            // Motion Values
            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ

            when (modeValue.get().lowercase()) {
                "packet" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = true)
                    sendCriticalPacket(ground = false)
                    sendCriticalPacket(yOffset = 1.1E-5, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "vanilla" -> {
                    val vanillaOffset = doubleArrayOf(0.11,0.1100013579,0.0000013579)
                    vanillaOffset.forEach { offset ->
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x ,y + offset, z, false))
                    }
                }

                "ncp2" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x, y + 0.11, z, false))
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1100013579, z, false))
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00114514, z, false))
                }

                "matrixsmart" -> {
                    counter++
                    if (counter > 3) {
                        sendCriticalPacket(yOffset = 0.110314, ground = false)
                        sendCriticalPacket(yOffset = 0.0200081, ground = false)
                        sendCriticalPacket(yOffset = 0.00000001300009, ground = false)
                        sendCriticalPacket(yOffset = 0.000000000022, ground = false)
                        sendCriticalPacket(ground = true)
                        counter = 0
                    }
                }

                "newpacket" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.05250000001304, z, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01400000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.thePlayer.onCriticalHit(entity)
                }

                "fixpacket" -> {
                    sendCriticalPacket(yOffset = 0.104080378093037,ground = false)
                    sendCriticalPacket(yOffset = 0.105454222033912,ground = false)
                    sendCriticalPacket(yOffset = 0.102888018147468,ground = false)
                    sendCriticalPacket(yOffset = 0.099634532004642,ground = false)
                }

                "mipacket" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "ncp" -> {
                    sendCriticalPacket(yOffset = 0.11, ground = false)
                    sendCriticalPacket(yOffset = 0.1100013579, ground = false)
                    sendCriticalPacket(yOffset = 0.0000013579, ground = false)
                }

                "aac5packet" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = false)
                    sendCriticalPacket(yOffset = 0.0433, ground = false)
                    sendCriticalPacket(yOffset = 0.2088, ground = false)
                    sendCriticalPacket(yOffset = 0.9963, ground = false)
                }

                "litepacket" -> {
                    sendCriticalPacket(yOffset = 0.015626, ground = false)
                    sendCriticalPacket(yOffset = 0.00000000343, ground = false)
                }

                "edit"->{
                    val ru = RandomUtils.getRandom(4.0E-7, 4.0E-5)
                    val doubleArray = arrayOf(0.007017625 + ru, 0.007349825 + ru, 0.006102874 + ru)
                    val n = doubleArray.size
                    var n2 = 0
                    while (n2 < n) {
                        val offset = doubleArray[n2]
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + offset, z, false))
                        ++n2
                    }
                }

                "kawaii" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00973333333333, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.001, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.01200000000007, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.0005, z, false))
                }

                "lowphase" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.0125, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01275, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 0.00025, z, true))
                }

                "noncalc" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E-5, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E-7, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E-6, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E-4, z, false))
                }

                "invalid" -> {  // WTF
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E+27, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E+68, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E+41, z, false))
                }

                "hpacket" -> {
                    // Hpacket ����
                    val hpv : DoubleArray
                    for (offset in doubleArrayOf(0.04132332, 0.023243243674, 0.01 ,0.0011).also { hpv = it }) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + offset, z, false))
                    }
                }

                "verussmart" -> {
                    counter++
                    if (counter == 1) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.001, z, true))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                    }
                    if (counter >= attackTimesValue.get())
                        counter = 0
                }

                "blocksmc" -> {
                //    if (!mc.thePlayer.onGround) return
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x, y + 0.003009028, z, false))
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x,y + 0.00010091981,z, true))
                    mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x,y,z,false))
                }

                "vulcan" -> {
                    counter++
                    if(counter > attackTimesValue.get()) {
                        sendCriticalPacket(yOffset = 0.2, ground = false)
                        sendCriticalPacket(yOffset = 0.1216, ground = false)
                        counter = 0
                    }
                }

                // Minemora criticals without test
                "minemora" -> {
                    sendCriticalPacket(yOffset = 0.0114514, ground = false)
                    sendCriticalPacket(yOffset = 0.0010999999940395355, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.0012016413, ground = false)
                }

                "aac4packet" -> {
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.01400000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                }

                "motion" -> {
                    when (motionValue.get().lowercase()) {
                        "jump" -> mc.thePlayer.motionY = 0.42
                        "tphop" -> {
                            sendCriticalPacket(yOffset = 0.02, ground = false)
                            sendCriticalPacket(yOffset = 0.01, ground = false)
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
                        }
                        "ncpsilent" -> {
                            sendPacket(0.42, false)
                            sendPacket(0.222, false)
                            sendPacket(0.0, true)
                        }
                        "lowjump" -> mc.thePlayer.motionY = 0.3425
                        "redeskylowhop" -> mc.thePlayer.motionY = 0.35
                        "hop" -> {
                            mc.thePlayer.motionY = 0.1
                            mc.thePlayer.fallDistance = 0.1f
                            mc.thePlayer.onGround = false
                        }
                        "aac5" -> {
                            mc.thePlayer.motionY = 0.42
                            mc.timer.timerSpeed = 0.8848f
                        }
                        "minemoratest" -> {
                            mc.timer.timerSpeed = 0.82f
                            mc.thePlayer.motionY = 0.124514
                        }
                    }
                }
            }
            msTimer.reset()
        }
    }

    @EventTarget
    fun onUpdate(event: MotionEvent) {
        if (modeValue.equals("Hypixel") && event.eventState == EventState.POST && event.onGround && LiquidBounce.combatManager.inCombat) {
            for (offset in doubleArrayOf(0.06, 0.01)) {
                val random = Math.random() * 0.001
                mc.thePlayer.sendQueue.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + offset + random,
                        mc.thePlayer.posZ,
                        false
                    )
                )
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            flagTimer.reset()
            if (s08FlagValue.get()) {
                jState = 0
            }
        }

        if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
            return

        if (packet is C03PacketPlayer) {
            when (modeValue.get().lowercase()) {
                "hypixeltest" -> {
                    packet.onGround = false
                }
                "redesky" -> {
                    val packetPlayer: C03PacketPlayer = packet as C03PacketPlayer
                    if(mc.thePlayer.onGround && canCrits) {
                        packetPlayer.y += 0.000001
                        packetPlayer.onGround = false
                    }
                    if(mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(
                            0.0, (mc.thePlayer.motionY - 0.08) * 0.98, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                        packetPlayer.onGround = true;
                    }
                    if (packet is C07PacketPlayerDigging) {
                        if(packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                            canCrits = false;
                        } else if(packet.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK || packet.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                            canCrits = true;
                        }
                    }
                }
                "edit2" -> {
                    if (readyCrits) {
                        packet.onGround = false
                        readyCrits = false
                    }
                }
                "noground" -> packet.onGround = false
                "aacnoGround" -> {
                    if(rsNofallValue.get()&&mc.thePlayer.fallDistance>0){
                        packet.onGround=true
                        return
                    }

                    if(mc.thePlayer.onGround && LiquidBounce.combatManager.inCombat && (packet is C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook)){
                        packet.onGround=false
                    }
                }
                "motion" -> {
                    when (motionValue.get().lowercase()) {
                        "minemoratest" -> if (!LiquidBounce.combatManager.inCombat) mc.timer.timerSpeed = 1.00f
                    }
                }
                "hover" -> {
                    if (hoverCombat.get() && !LiquidBounce.combatManager.inCombat) return
                    packet.isMoving = true
                    when (hoverValue.get().lowercase()) {
                        "2b2t" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.02
                                    3 -> packet.y += 0.01
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis2" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.015625
                                } else if (jState> 100) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "tpcollide" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.20000004768372
                                    3 -> packet.y += 0.12160004615784
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.0625
                                } else if (jState> 50) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "normal1" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.001335979112147
                                    3 -> packet.y += 0.0000000131132
                                    4 -> packet.y += 0.0000000194788
                                    5 -> packet.y += 0.00000000001304
                                    6 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "normal3" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.001335979114514
                                    3 -> packet.y += 0.0000005235532
                                    4 -> packet.y += 0.0000000194788
                                    5 -> packet.y += 0.00000000001314
                                    6 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "aac4other" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.00101
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.001
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "aac4" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.000000000000136
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.000000000000036
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "normal2" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.00000000000667547
                                    3 -> packet.y += 0.00000000000045413
                                    4 -> packet.y += 0.000000000000036
                                    5 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "oldredesky" -> {
                            if (hoverNoFall.get() && mc.thePlayer.fallDistance> 0) {
                                packet.onGround = true
                                return
                            }

                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                            }
                        }
                    }
                }
            }
        }
        if (packet is S0BPacketAnimation) {
            if (packet.animationType == 4 && packet.entityID == target) {
                when (debugValue.get().lowercase()) {
                    "chata" -> alert("Critical!")
                    "chatb" -> alert("Critical:" + RandomUtils.randomNumber(4))
                    "notia" -> LiquidBounce.hud.addNotification(Notification("Debug", "Critical!  ", NotifyType.INFO))
                    "notib" -> LiquidBounce.hud.addNotification(Notification("Debug", "Critical:" + RandomUtils.randomNumber(4) + "      ", NotifyType.INFO))
                    "chatc" -> alert("Critical:" + RandomUtils.randomNumber(4) + "." + RandomUtils.randomNumber(4))
                    "chatd" -> alert("Critical:" + RandomUtils.randomNumber(3) + "." + RandomUtils.randomNumber(6))
                    "notic" -> LiquidBounce.hud.addNotification(Notification("Debug", "Critical:" + RandomUtils.randomNumber(4) + "." + RandomUtils.randomNumber(2) + "       ", NotifyType.INFO))
                    "notid" -> LiquidBounce.hud.addNotification(Notification("Debug", "Critical:" + RandomUtils.randomNumber(4) + "." + RandomUtils.randomNumber(4) + "         ", NotifyType.INFO))
                }
            }
        }
    }

    override val tag: String
        get() = "${modeValue.get()} ${delayValue.get()}"

    fun sendPacket(y: Double, ground: Boolean) {
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ, ground))
    }
}
