/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.bugfixes.crashes;

import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

// MC-81738 (todo: document probably, this makes no sense at first glance)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin_ResolveCrash {

    @Shadow private IntBuffer rawIntBuffer;
    @Shadow private VertexFormat vertexFormat;

    @Inject(method = "finishDrawing", at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;limit(I)Ljava/nio/Buffer;", remap = false))
    private void patcher$resetBuffer(CallbackInfo ci) {
        this.rawIntBuffer.position(0);
        ClientUtils.INSTANCE.tipException("渲染世界时出错，已修复渲染。");
    }

    @Inject(method = "endVertex", at = @At("HEAD"))
    private void patcher$adjustBuffer(CallbackInfo ci) {
        this.rawIntBuffer.position(this.rawIntBuffer.position() + this.vertexFormat.getIntegerSize());
        ClientUtils.INSTANCE.tipException("渲染世界时出错，已修复渲染。");
    }
}
