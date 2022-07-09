package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.TextValue
import org.lwjgl.opengl.Display

@ModuleInfo(name = "CustomTitle", ModuleCategory.ADDIT)
class CustomTitle : Module() {
    val titleValue = TextValue("Title", "Drama Build 114514 | Bypass Watchdog Fly")

    override fun onEnable() {
        Display.setTitle(titleValue.get())
    }
}