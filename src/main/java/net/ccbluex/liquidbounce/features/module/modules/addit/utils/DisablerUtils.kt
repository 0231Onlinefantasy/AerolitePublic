package net.ccbluex.liquidbounce.features.module.modules.addit.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.world.BlockFly
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C03PacketPlayer
import java.util.concurrent.ThreadLocalRandom

object DisablerUtils {
    fun getRandom(min: Double, max: Double): Double {
        var min = min
        var max = max
        if (min == max) {
            return min
        } else if (min > max) {
            val d = min
            min = max
            max = d
        }
        return ThreadLocalRandom.current().nextDouble(min, max)
    }

}