package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixHopB : SpeedMode("Matrix6.7.0") {
    private var noVelocityY = 0

    override fun onUpdate() {
        if (noVelocityY >= 0) {
            noVelocityY = noVelocityY - 1
        }
        if (!mc.thePlayer.onGround && noVelocityY <= 0) {
            if (mc.thePlayer.motionY > 0) {
                mc.thePlayer.motionY -= 0.0005
            }
            mc.thePlayer.motionY -= 0.0094001145141919810
        }
        if (!mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            if (MovementUtils.getSpeed() < 0.2177 && noVelocityY < 8) {
                MovementUtils.strafe(0.2177f)
            }
        }
        if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
            mc.thePlayer.jumpMovementFactor = 0.026f
        }else{
            mc.thePlayer.jumpMovementFactor = 0.0247f
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.41050001145141919810
            if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
                MovementUtils.strafe()
            }
        }
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        noVelocityY = 0
    }
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            noVelocityY = 10
        }
    }
}