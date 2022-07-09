//我操你妈的更改抽打hyp操
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class WatchDogNormal : SpeedMode("WatchDogNormal") {
    override fun onEnable() {
        mc.thePlayer.speedInAir = 0.045f

        mc.timer.timerSpeed = 1.1f

    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f

        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.3
            mc.thePlayer.speedInAir = 0.045f
            mc.timer.timerSpeed = 1.1f

            MovementUtils.strafe()


        } else {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ

            mc.thePlayer.speedInAir = 0.043f

            mc.timer.timerSpeed = 1f
        }
    }
}
//