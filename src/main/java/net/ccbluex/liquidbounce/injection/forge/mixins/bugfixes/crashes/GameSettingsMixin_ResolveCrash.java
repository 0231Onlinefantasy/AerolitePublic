/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.bugfixes.crashes;

import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameSettings.class)
public class GameSettingsMixin_ResolveCrash {

    /**
     * @author asbyth
     * @reason Resolve Chat Key bound to a unicode char causing crashes while creative inventory is opened (MC-102867)
     */
    @Overwrite
    public static boolean isKeyDown(KeyBinding key) {
        int keyCode = key.getKeyCode();
        ClientUtils.INSTANCE.tipException("你的键盘输入了Unicode字符导致了游戏的崩溃(MC-102867)，已取消该事件。");
        if (keyCode != 0 && keyCode < 256) {
            return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
        } else {
            return false;
        }
    }
}
