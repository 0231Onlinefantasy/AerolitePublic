/*
 * AeroLite Client
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RenderUtil
import net.minecraft.entity.item.EntityTNTPrimed
import java.awt.Color

@ModuleInfo(name = "TNTESP", category = ModuleCategory.VISUAL)
class TNTESP : Module() {

    @EventTarget
    fun onRender3D(event : Render3DEvent) {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityTNTPrimed>().forEach { RenderUtil.drawEntityBox(it, Color.RED, false) }
    }

    // Where? I cant find my way home... By Stars
}