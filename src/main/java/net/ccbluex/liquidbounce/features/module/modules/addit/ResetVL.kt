package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "ResetVL", category = ModuleCategory.ADDIT)
class ResetVL : Module() {
    private var jumped = 0
    private val y = 0.0
    @EventTarget
    fun onMotion(event: MotionEvent?) {
        if (mc.thePlayer.onGround) {
            if (jumped <= 25) {
                mc.thePlayer.motionY = 0.11
                jumped++
            }
        }
        if (jumped <= 25) {
            mc.thePlayer.posY = y
            mc.timer.timerSpeed = 1f
        }
    }

    override fun onEnable() {
        jumped = 0
        super.onEnable()
    }
}