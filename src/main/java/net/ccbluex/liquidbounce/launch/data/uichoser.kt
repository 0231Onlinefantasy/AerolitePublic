package net.ccbluex.liquidbounce.launch.data

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.ccbluex.liquidbounce.launch.data.legacyui.GuiMainMenu
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.Display
import java.awt.Color
import javax.swing.JOptionPane

class uichoser : GuiScreen() {
    override fun initGui() {
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, height / 2 - 20, 120, 20, "HACK NOW"))
    }



    override fun drawScreen(mouseX: Int, mouseY: Int, pTicks: Float) {
        mc.textureManager.bindTexture(ResourceLocation("aerolite/main/game.png"))
        drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())

        drawCenteredString(mc.fontRendererObj, "Welcome,User", width / 2, height / 2 - 40, Color.green.rgb)

        super.drawScreen(mouseX, mouseY, pTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        LiquidBounce.launchFilters.addAll(when (button.id) {
            0 -> arrayListOf(EnumLaunchFilter.LEGACY_UI)
            else -> arrayListOf(EnumLaunchFilter.LEGACY_UI) })
        LiquidBounce.startClient()

        if (mc.currentScreen is uichoser) {
            mc.displayGuiScreen(LiquidBounce.mainMenu)
        }
    }

    override fun keyTyped(p_keyTyped_1_: Char, p_keyTyped_2_: Int) { }
}
