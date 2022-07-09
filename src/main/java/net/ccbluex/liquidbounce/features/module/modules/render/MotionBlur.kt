package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.ClickGui
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.ResourceLocation

@ModuleInfo("MotionBlur",  ModuleCategory.RENDER)
class MotionBlur : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Little","Normal","Large"),"Little")

    override fun onDisable() {
        mc.entityRenderer.stopUseShader()
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        if(mc.currentScreen !is ClickGui) {
            when (modeValue.get()) {
                "Little" -> {
                    mc.entityRenderer.loadShader(ResourceLocation("aerolite/shader/blur/little.json"))
                }
                "Normal" -> {
                    mc.entityRenderer.loadShader(ResourceLocation("aerolite/shader/blur/normal.json"))
                }
                "Large" -> {
                    mc.entityRenderer.loadShader(ResourceLocation("aerolite/shader/blur/large.json"))
                }
            }
        } else
            mc.entityRenderer.stopUseShader()
    }

}