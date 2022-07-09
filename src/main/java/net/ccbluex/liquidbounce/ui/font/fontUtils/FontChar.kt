package net.ccbluex.liquidbounce.ui.font.fontUtils

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

/**
 * @param char 对应的字符
 * @param bufImg 渲染出的字符图片
 * 为了支持UTF16，所以char是string
 */
class FontChar(val char: String, val resourceLocation: ResourceLocation, val bufImg: BufferedImage) {
    val width=bufImg.width

    init {
        // this need to run on minecraft main thread
        val mc=Minecraft.getMinecraft()
        mc.addScheduledTask {
            mc.textureManager.loadTexture(resourceLocation, DynamicTexture(bufImg))
        }
    }
}