package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "InfinitePotion", category = ModuleCategory.ADDIT)
class InfinitePotion : Module(){
    var isDrink = false
    var count = 0
    var offsetY = 0
    var dur = 32

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if (isDrink) {
            count++
            mc.thePlayer.rotationPitch = 90F
            if ((count + 4) == dur) {
                val posX = mc.thePlayer.posX
                val posY = mc.thePlayer.posY
                val posZ = mc.thePlayer.posZ
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(posX, posY + 4.0, posZ, true))
                LiquidBounce.hud.notifications.add(Notification("Infinite Potion", "Trying to send glitch packet...", NotifyType.INFO))
            }
        }
        if (mc.thePlayer.isUsingItem) {
            val playerItem = mc.thePlayer.itemInUse.item
            if (!isDrink) {
                if (playerItem is ItemPotion) {
                    mc.thePlayer.rotationPitch = mc.thePlayer.rotationPitch
                    ClientUtils.displayChatMessage("§c§lWarning! You are sending AAC5 glitch packet,please don't rotate or move!!")
                    isDrink = true
                }
            }
        } else {
            if (isDrink) {
                isDrink = false
                offsetY = 0
                count = 0
            }
        }
    }

    override val tag: String
        get() = "HytPacket"

}