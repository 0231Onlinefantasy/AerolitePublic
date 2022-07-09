package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.minecraft.block.Block
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

object PlayerUtils {
    fun randomUnicode(str: String): String {
        val stringBuilder = StringBuilder()
        for (c in str.toCharArray()) {
            if (Math.random()> 0.5 && c.code in 33..128) {
                stringBuilder.append(Character.toChars(c.code + 65248))
            } else {
                stringBuilder.append(c)
            }
        }
        return stringBuilder.toString()
    }
    fun isUsingFood(): Boolean {
        val usingItem = mc.thePlayer.itemInUse.item
        return if (mc.thePlayer.itemInUse != null) {
            mc.thePlayer.isUsingItem && (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion)
        } else false
    }
    fun isBlockUnder(): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.getEntityBoundingBox()
                .offset(0.0, -off.toDouble(), 0.0)
            if (mc.theWorld.getCollidingBoundingBoxes(
                    mc.thePlayer,
                    bb
                ).isNotEmpty()
            ) {
                return true
            }
            off += 2
        }
        return false
    }
    fun getBlockRelativeToPlayer(offsetX: Double, offsetY: Double, offsetZ: Double): Block? {
        return mc.theWorld.getBlockState(
            BlockPos(
                mc.thePlayer.posX + offsetX,
                mc.thePlayer.posY + offsetY,
                mc.thePlayer.posZ + offsetZ
            )
        ).block
    }
}