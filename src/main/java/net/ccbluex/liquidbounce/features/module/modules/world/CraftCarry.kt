package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C0DPacketCloseWindow

@ModuleInfo(name = "CraftCarry", category = ModuleCategory.WORLD)
class CraftCarry : Module() {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C0DPacketCloseWindow)
            event.cancelEvent()
    }
}