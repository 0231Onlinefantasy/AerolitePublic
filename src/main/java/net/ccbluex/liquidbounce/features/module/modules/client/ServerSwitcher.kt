/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiMainMenu


@ModuleInfo(name = "ServerSwitcher", category = ModuleCategory.CLIENT, canEnable = false)
class ServerSwitcher : Module() {

    override fun onEnable() {
        mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
    }
} 