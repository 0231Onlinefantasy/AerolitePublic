package net.ccbluex.liquidbounce.features.module.modules.addit

import me.stars.fxc.GuiDEATH
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo("Death", ModuleCategory.ADDIT, canEnable = false)
class Death : Module() {

    override fun onEnable() {
        mc.displayGuiScreen(GuiDEATH())
        super.onEnable()
    }
}