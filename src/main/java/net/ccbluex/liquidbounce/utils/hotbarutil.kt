package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.utils.RenderUtil
import me.stars.utils.TransUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

class hotbarutil {
    val translate = TransUtils(0f , 0f)
    var size = 1.0f

    fun renderHotbarItem(index: Int, xPos: Float, yPos: Float, partialTicks: Float) {

        val itemStack = mc.thePlayer.inventory.mainInventory[index]
        if (itemStack != null) {
            val lvt_7_1_ = itemStack.animationsToGo.toFloat() - partialTicks
            if (lvt_7_1_ > 0.0f) {
                GlStateManager.pushMatrix()
                val lvt_8_1_ = 1.0f + lvt_7_1_ / 5.0f
                GlStateManager.translate((xPos + 8).toFloat(), (yPos + 12).toFloat(), 0.0f)
                GlStateManager.scale(1.0f / lvt_8_1_, (lvt_8_1_ + 1.0f) / 2.0f, 1.0f)
                GlStateManager.translate((-(xPos + 8)).toFloat(), (-(yPos + 12)).toFloat(), 0.0f)
            }
            RenderUtil.drawTexturedRect(xPos - 7, yPos - 7, 30f, 30f, "hotbar", ScaledResolution(mc));
            RenderUtil.drawTexturedRect(xPos - 7, yPos - 7, 30f, 30f, "hotbar", ScaledResolution(mc));
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, xPos.toInt(), yPos.toInt())
            if (lvt_7_1_ > 0.0f) {
                GlStateManager.popMatrix()
            }
            mc.renderItem.renderItemOverlays(Fonts.font35, itemStack, xPos.toInt(), yPos.toInt())
        }
    }
}