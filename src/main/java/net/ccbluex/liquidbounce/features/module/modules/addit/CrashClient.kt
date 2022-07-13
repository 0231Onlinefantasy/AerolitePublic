package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import org.lwjgl.opengl.Display

@ModuleInfo("CrashClient", ModuleCategory.ADDIT)
class CrashClient : Module() {
    override fun onEnable() {
        Display.destroy()
    }
}