package net.ccbluex.liquidbounce.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class SkidUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static int worldChanges;

    public static Block getBlockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + offsetX, mc.thePlayer.posY + offsetY, mc.thePlayer.posZ + offsetZ)).getBlock();
    }

    public static boolean generalAntiPacketLog() {
        return worldChanges > 1;
    }
}
