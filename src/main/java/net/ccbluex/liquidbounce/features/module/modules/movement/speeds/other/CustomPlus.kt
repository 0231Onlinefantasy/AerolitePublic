/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sqrt

class CustomPlus : SpeedMode("CustomPlus") {
    private val speedValue = FloatValue("StrafeSpeed", 1.6f, 0.2f, 2f)
    private val launchSpeedValue = FloatValue("LaunchSpeed", 1.6f, 0.2f, 2f).displayable {doLaunchSpeedValue.get()}
    private val addYMotionValue = FloatValue("AddYMotion", 0f, 0f, 2f)
    private val yValue = FloatValue("YMotion", 0f, 0f, 4f)
    private val upTimerValue = FloatValue("UpTimer", 1f, 0.1f, 2f)
    private val downTimerValue = FloatValue("DownTimer", 1f, 0.1f, 2f)
    private val strafeEnableValue = BoolValue("Strafe", true)
    private val strafeValue = ListValue("CustomStrafe", arrayOf("Strafe", "Boost", "Plus", "PlusOnlyUp", "Non-Strafe"), "Boost").displayable {strafeEnableValue.get()}
    private val groundStay = IntegerValue("GroundStayTicks", 0, 0, 10)
    private val groundResetXZValue = BoolValue("GroundResetXZ", false)
    private val resetXZValue = BoolValue("ResetXZ", false)
    private val resetYValue = BoolValue("ResetY", false)
    private val doLaunchSpeedValue = BoolValue("DoLaunchSpeed", true)
    private val airSpeedValue = FloatValue("AirSpeed", 0.02F, 0.01F, 1F)
    private val blinkValue = BoolValue("Blink", false)
    private val setKeyValue = BoolValue("SetKey", false)
    private val cancelVelocityValue = BoolValue("CancelVelocity", false)
    private val velocityBoostValue = BoolValue("VelocityBoost", true)
    private val velocityYValue = FloatValue("VelocityY", 5F, 1F, 10F).displayable {velocityBoostValue.get()}
    private val velocityXZValue = FloatValue("VelocityXZ", 5F, 1F, 10F).displayable {velocityBoostValue.get()}
    private val velocitySpeedAddValue = FloatValue("VelocitySpeedAdd", 0.1F, 0F, 1F).displayable {velocityBoostValue.get()}
    private val velocityTimerAddValue = FloatValue("VelocityTimerAdd", 0F, 0F, 2F).displayable {velocityBoostValue.get()}
    private val flagCheckValue = BoolValue("FlagCheck", true)
    private val ignoreGhostBlockValue = BoolValue("IgnoreGhostBlock", true)

    private var groundTick = 0

    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY> 0) { upTimerValue.get() } else { downTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= groundStay.get()) {
                        if (doLaunchSpeedValue.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (yValue.get() != 0f) {
                            mc.thePlayer.motionY = yValue.get().toDouble()
                        }
                    } else if (groundResetXZValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                    }
                    groundTick++
                }
                else -> {
                    groundTick = 0
                    if (strafeEnableValue.get()) {
                    when (strafeValue.get().lowercase()) {
                        "strafe" -> MovementUtils.strafe(speedValue.get())
                        "boost" -> MovementUtils.strafe()
                        "plus" -> MovementUtils.move(speedValue.get() * 0.1f)
                        "plusonlyup" -> if (mc.thePlayer.motionY> 0) {
                            MovementUtils.move(speedValue.get() * 0.1f)
                        } else {
                            MovementUtils.strafe()
                        }
                    }
                    mc.thePlayer.motionY += addYMotionValue.get() * 0.03
                }
            }
            }
        } else if (resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    fun onUpdate(event: UpdateEvent) {
        LiquidBounce.moduleManager[Blink::class.java]!!.state = blinkValue.get()
        if (mc.thePlayer.onGround && setKeyValue.get()) {
            mc.gameSettings.keyBindJump.pressed = true
        }
        if (!mc.thePlayer.onGround && setKeyValue.get()) {
            mc.gameSettings.keyBindJump.pressed = false
        }
    }

    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val timerBeforeBoost = mc.timer.timerSpeed
         if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            if (cancelVelocityValue.get()) event.cancelEvent()

            val recX = packet.motionX / (velocityXZValue.get() * 1000)
            val recZ = packet.motionZ / (velocityXZValue.get() * 1000)
            if(sqrt(recX*recX+recZ*recZ) > MovementUtils.getSpeed()) {
                MovementUtils.strafe(sqrt((recX*recX+recZ*recZ).toDouble()).toFloat())
                mc.thePlayer.motionY = (packet.motionY / (velocityYValue.get() * 1000)).toDouble()
            }

            MovementUtils.strafe((MovementUtils.getSpeed()*(1 + velocitySpeedAddValue.get())))

             mc.timer.timerSpeed = timerBeforeBoost + velocityTimerAddValue.get()
        } else {
             mc.timer.timerSpeed = timerBeforeBoost
         }
        if (flagCheckValue.get() && packet is S08PacketPlayerPosLook && mc.theWorld != null) {
            ClientUtils.customAlert("[Speed] Flag! S08 Detect!")
        }
        if (!mc.thePlayer.onGround && packet is S08PacketPlayerPosLook && mc.theWorld != null || mc.thePlayer.isEntityInsideOpaqueBlock) {
            mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.motionX - 0.7, mc.thePlayer.motionY + 0.1, mc.thePlayer.motionZ - 0.7, true))
            LiquidBounce.moduleManager[Speed::class.java]!!.state = false
        }
    }

    override fun onEnable() {
        if (resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (resetYValue.get()) mc.thePlayer.motionY = 0.0
        mc.thePlayer.speedInAir = airSpeedValue.get()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
        if (blinkValue.get()) {
            LiquidBounce.moduleManager[Blink::class.java]!!.state = false
        }
    }
}