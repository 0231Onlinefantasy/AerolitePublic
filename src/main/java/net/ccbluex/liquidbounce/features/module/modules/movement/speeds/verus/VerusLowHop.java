/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus;

import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class VerusLowHop extends SpeedMode {

    public VerusLowHop() {
        super("VerusLowHop");
    }

    public void onMotion() {
    }
    @Override
    public void onUpdate() {
    }
    @Override
    public void onMove(MoveEvent event) {
        if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava() && !mc.thePlayer.isInWater() && !mc.thePlayer.isOnLadder() && mc.thePlayer.ridingEntity == null) {
            if (MovementUtils.INSTANCE.isMoving()) {
                mc.gameSettings.keyBindJump.pressed = false;
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionY = 0;
                    MovementUtils.INSTANCE.strafe(0.61F);
                    event.setY(0.41999998688698);
                }
                MovementUtils.INSTANCE.strafe();
            }
        }
    }
}
