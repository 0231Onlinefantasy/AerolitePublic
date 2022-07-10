package net.ccbluex.liquidbounce.features.module.modules.addit

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display

@ModuleInfo(name = "CustomTitle", ModuleCategory.ADDIT)
class CustomTitle : Module() {
    val titleValue = TextValue("Title", "Liquidbounce b73")

    var wasRestarted = false

    override fun onEnable() {
        Display.setTitle(titleValue.get())
    }

    fun onTick(event: TickEvent) {
        if (!wasRestarted) {
            Display.setTitle(titleValue.get())
            wasRestarted = true
        }
    }
}