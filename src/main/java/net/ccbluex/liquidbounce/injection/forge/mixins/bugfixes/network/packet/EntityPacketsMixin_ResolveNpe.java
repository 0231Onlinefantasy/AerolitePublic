/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.bugfixes.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({S14PacketEntity.class, S19PacketEntityHeadLook.class, S19PacketEntityStatus.class})
public class EntityPacketsMixin_ResolveNpe {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
        method = {"getEntity", "func_149065_a", "func_149381_a", "func_149161_a"},
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void patcher$addNullCheck(World worldIn, CallbackInfoReturnable<Entity> cir) {
        if (worldIn == null) {
            cir.setReturnValue(null);
        }
    }
}
