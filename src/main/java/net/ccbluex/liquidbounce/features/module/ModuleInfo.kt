/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import org.lwjgl.input.Keyboard

@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleInfo(
    val name: String,
    val category: ModuleCategory,
    val keyBind: Int = Keyboard.CHAR_NONE,
    val canEnable: Boolean = true,
    val array: Boolean = true,
    val autoDisable: EnumAutoDisableType = EnumAutoDisableType.NONE,
    val moduleCommand: Boolean = true,
    val defaultOn: Boolean = false,
    val triggerType: EnumTriggerType = EnumTriggerType.TOGGLE
)

enum class EnumAutoDisableType {
    NONE,
    RESPAWN,
    FLAG,
    GAME_END
}

enum class EnumTriggerType {
    TOGGLE,
    PRESS
}