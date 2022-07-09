/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.bugfixes;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

import java.util.*;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin_DuplicatedSounds {
    @Shadow public abstract boolean isSoundPlaying(ISound sound);

    @Shadow @Final private Map<String, ISound> playingSounds;

    private final List<String> patcher$pausedSounds = new ArrayList<>();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Redirect(
        method = "pauseAllSounds",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;pause(Ljava/lang/String;)V", remap = false)
    )
    private void patcher$onlyPauseSoundIfNecessary(@Coerce SoundSystem soundSystem, String sound) {
        if (isSoundPlaying(playingSounds.get(sound))) {
            soundSystem.pause(sound);
            patcher$pausedSounds.add(sound);
        }
    }

    @Redirect(
        method = "resumeAllSounds",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false)
    )
    private Iterator<String> patcher$iterateOverPausedSounds(Set<String> keySet) {
        return patcher$pausedSounds.iterator();
    }

    @Inject(method = "resumeAllSounds", at = @At("TAIL"))
    private void patcher$clearPausedSounds(CallbackInfo ci) {
        patcher$pausedSounds.clear();
    }
}
