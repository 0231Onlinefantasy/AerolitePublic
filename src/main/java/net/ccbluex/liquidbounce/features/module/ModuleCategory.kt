/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.cn.Fonts.newdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.cn.Fonts.newdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.cn.Fonts.newdropdown.utils.render.Scroll


enum class ModuleCategory(val displayName: String, val configName: String, val NotiModule: String) {
    COMBAT("%module.category.combat%", "Combat", "a"),
    PLAYER("%module.category.player%", "Player", "d"),
    MOVEMENT("%module.category.movement%", "Movement", "b"),
    RENDER("%module.category.render%", "Render", "s"),
    CLIENT("%module.category.client%", "Client", "q"),
    WORLD("%module.category.world%", "World",  "e"),
    MISC("%module.category.misc%", "Misc", "m"),
    EXPLOIT("%module.category.exploit%", "Exploit", "f"),
    ADDIT("%module.category.addit%", "Addit", "u"),
    VISUAL("%module.category.visual%", "Visual", "s");

    var namee: String? = null
    var posX = 0
    var expanded = false

    private var scroll = Scroll()

    open fun getScroll(): Scroll {
        return scroll
    }

    private var drag = Drag()

    open fun getDrag(): Drag {
        return drag
    }

    var posY = 20


    open fun ModuleCategory(name: String?) {
        namee = name
        posX = 40 + Main.categoryCount * 120
        drag = Drag(posX.toFloat(), posY.toFloat())
        expanded = true
        Main.categoryCount++
    }
    }

enum class NotiInfo(val NotiModule: String){
    COMBAT( "a"),
    PLAYER( "d"),
    MOVEMENT( "b"),
    RENDER( "s"),
    CLIENT("q"),
    WORLD( "e"),
    MISC( "m"),
    EXPLOIT( "f"),
    ADDIT("u"),
    VISUAL("s");
}


