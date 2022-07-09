/*
   Script by ys_
   Recovered to kotlin by Stars
   AeroLite Hack Client - Best LiquidBounce Hack
   Modified with LOVE by Stars&Packet:(
   https://gitee.com/starslight/aero-lite
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "RedeLongjump", category = ModuleCategory.MOVEMENT)
class RedeLongjump : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Old", "New"), "New")
    private val MinMotionWork = FloatValue("MinMotionWork", 0.1F, -0.5F, 0.5F).displayable { modeValue.equals("New") }
    private val ticksValue = IntegerValue("Ticks", 21, 1, 25).displayable { modeValue.equals("Old") }
    private val timerValue = FloatValue("Timer", 1.5F, 0.1F, 3.5F).displayable { modeValue.equals("Old") }
    private val xzMultiplier = FloatValue("XZ-Multiplier", 0.9F, 0.1F, 1F).displayable { modeValue.equals("Old") }
    private val yMultiplier = FloatValue("Y-Multiplier", 0.77F, 0.1F, 1F).displayable { modeValue.equals("Old") }
    private val glide = BoolValue("Glide", true).displayable { modeValue.equals("Old") }

    private var jumped = false
    private var flag = false
    private var ticks = 0

    @EventTarget
    override fun onEnable() {
        jumped = false
        flag = false
        ticks = 0 // for old mode,but did not equal.
        mc.timer.timerSpeed = 1.0F
    }

    @EventTarget
    override fun onDisable() {
        jumped = false;
        flag = false;
        mc.timer.timerSpeed = 1.0F
        mc.thePlayer.motionX *= 0.999999998;
        mc.thePlayer.motionZ *= 0.999999998;
    }

    @EventTarget
    fun onUpdate(Event: UpdateEvent) {
        if (modeValue.get().equals("New")) {
            if (!mc.thePlayer.onGround && mc.thePlayer.motionY >= MinMotionWork.get() && !flag) {
                mc.thePlayer.motionY += 0.059899999999;
                mc.thePlayer.motionX *= 1.08;
                mc.thePlayer.motionZ *= 1.08;
            }
            if (!jumped && mc.thePlayer.onGround && (mc.thePlayer.motionX != 0.0 || mc.thePlayer.motionZ != 0.0)) {
                mc.thePlayer.jump();
                jumped = true;
            }
        }
        // TODO: This mode is not used in the script, but i change it back lol
        if (modeValue.get().equals("Old")) {
            if (ticks < ticksValue.get()) {
                mc.timer.timerSpeed = timerValue.get();
                mc.thePlayer.motionY *= yMultiplier.get();
                mc.thePlayer.motionX *= xzMultiplier.get();
                mc.thePlayer.motionZ *= xzMultiplier.get();

                mc.thePlayer.jump();
            }
            if (glide.get()) {
                mc.thePlayer.motionY += 0.03;
            }
            ticks++;
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook) {
            flag = true;
        }
    }
}
// Recovered By Stars