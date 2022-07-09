package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "AntiFire", category = ModuleCategory.PLAYER)
class AntiFire : Module() {
    private val packetsValue = IntegerValue("Packets", 10,1,50)
    private val noAirValue = BoolValue("NoAir", true)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.capabilities.isCreativeMode && mc.thePlayer.isBurning && (mc.thePlayer.onGround && noAirValue.get())) {
            var i = 0
            while (i < packetsValue.get()) {
                mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                i++
            }
        }
    }
}