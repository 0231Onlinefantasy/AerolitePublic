package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class BmcBhop : SpeedMode("BmcBhop") {
    private val strafeboost = FloatValue("StrafeBoost", 0.001f, 0f, 0.009f)
    private val jumpboost = FloatValue("JumpBoost", 0.001f, 0f, 0.009f)
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
            if (MovementUtils.isMoving()) {
                mc.gameSettings.keyBindJump.pressed = false
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    MovementUtils.strafe(0.481f + strafeboost.get())
                    mc.timer.timerSpeed = 1.066f
                    mc.thePlayer.jumpMovementFactor = 0.02f
                }
                mc.timer.timerSpeed = 1.034f
                MovementUtils.strafe()
            }
            if (MovementUtils.isOnGround(0.2)) {
                mc.timer.timerSpeed = 1.15f
            }
            if (mc.thePlayer.fallDistance > 0.7) {
                mc.thePlayer.jumpMovementFactor = 0.021f + jumpboost.get()
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.jumpMovementFactor = 0.02f
    }
}