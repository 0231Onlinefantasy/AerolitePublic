//死妈仔暴打hyp试了合适的噶话给大家按时
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class WatchDogFast : SpeedMode("WatchDogFast") {
    override fun onEnable() {

        mc.thePlayer.speedInAir = 0.0425f

        mc.timer.timerSpeed = 1.04f

    }

    override fun onDisable() {

        mc.thePlayer.speedInAir = 0.02f

        mc.timer.timerSpeed = 1f

    }

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {

            if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.3

            mc.thePlayer.speedInAir = 0.0425f

            mc.timer.timerSpeed = 1.04f

            MovementUtils.strafe()

        } else {

            mc.thePlayer.motionZ = 0.0

            mc.thePlayer.motionX = mc.thePlayer.motionZ

            mc.thePlayer.speedInAir = 0.02f

            mc.timer.timerSpeed = 1f

        }
    }
}
//