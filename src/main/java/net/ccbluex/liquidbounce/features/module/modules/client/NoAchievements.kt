/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "NoAchievements", category = ModuleCategory.CLIENT, array = false)
class NoAchievements : Module() {
    private val delayValue = IntegerValue("CheckDelay", 3000, 0, 10000)
    private val logValue = BoolValue("LogInfo", true)
    val abc = MSTimer()
    @EventTarget
    fun onTick(event: TickEvent) {
        if (abc.hasTimePassed(delayValue.get().toLong())) {
            mc.guiAchievement.clearAchievements()
            if (logValue.get()) ClientUtils.displayAlert(delayValue.get().toString() + "ms passed. Reset all achievements.")
            abc.reset()
        }
    }

    override fun onEnable() {
        if (logValue.get()) LiquidBounce.hud.addNotification(Notification("NoAchievements", "Closed all achievements!", NotifyType.SUCCESS))
    }
}
