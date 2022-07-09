/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import de.enzaxd.viaforge.ViaForge;
//import de.enzaxd.viaforge.gui.GuiProtocolSelector;
import de.enzaxd.viaforge.platform.ViaAPI;
import de.enzaxd.viaforge.protocol.ProtocolCollection;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.GuiAntiForge;
import net.ccbluex.liquidbounce.ui.client.GuiProxySelect;
import net.ccbluex.liquidbounce.ui.client.GuiServerSpoof;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {

    private GuiSlider viaSlider;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        buttonList.add(new GuiButton(997, 5, 8, 98, 20, "%ui.antiForge%"));
        buttonList.add(new GuiButton(998, width - 104, 8, 98, 20, "%ui.serverSpoof%"));
        buttonList.add(new GuiButton(999, width - 208, 8, 98, 20, "Proxy"));


        buttonList.add(viaSlider = new GuiSlider(1337, width - 312, 8, 98, 20, "Version: ", "", 0, ProtocolCollection.values().length - 1, ProtocolCollection.values().length - 1 - getProtocolIndex(ViaForge.getInstance().getVersion()), false, true,
                guiSlider -> {
                    ViaForge.getInstance().setVersion(ProtocolCollection.values()[ProtocolCollection.values().length - 1 - guiSlider.getValueInt()].getVersion().getVersion());
                    this.updatePortalText();
                }));




    }


    private int getProtocolIndex(int id) {
        for (int i = 0; i < ProtocolCollection.values().length; i++)
            if (ProtocolCollection.values()[i].getVersion().getVersion() == id)
                return i;
        return -1;
    }


    private void updatePortalText() {
        if (this.viaSlider == null)
            return;

        this.viaSlider.displayString = "Version: " + ProtocolCollection.getProtocolById(ViaForge.getInstance().getVersion()).getName();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        switch(button.id) {
            case 997:
                mc.displayGuiScreen(new GuiAntiForge((GuiScreen) (Object) this));
                break;
            case 998:
                mc.displayGuiScreen(new GuiServerSpoof((GuiScreen) (Object) this));
                break;
            case 999:
                mc.displayGuiScreen(new GuiProxySelect((GuiScreen) (Object) this));
                break;
        }
    }

    @Inject(method="connectToServer", at=@At(value="HEAD"))
    public void connectToServer(CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getNetHandler() != null) {
            minecraft.getNetHandler().getNetworkManager().closeChannel(new ChatComponentText(""));
        }
    }
}