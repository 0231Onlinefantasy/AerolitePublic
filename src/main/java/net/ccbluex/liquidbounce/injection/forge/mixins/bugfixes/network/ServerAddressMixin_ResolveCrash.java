/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.bugfixes.network;

import net.minecraft.client.multiplayer.ServerAddress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.IDN;

@Mixin(ServerAddress.class)
public class ServerAddressMixin_ResolveCrash {
    @Shadow @Final private String ipAddress;

    /**
     * @author LlamaLad7
     * @reason Fix crash - MC-89698
     */
    @Overwrite
    public String getIP() {
        try {
            return IDN.toASCII(this.ipAddress);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
