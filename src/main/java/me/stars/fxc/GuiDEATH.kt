package me.stars.fxc

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import java.awt.Color
// V2.2 By Stars
class GuiDEATH : GuiScreen() {

    override fun initGui() {
        buttonList.add(GuiButton(0, this.width / 2 - 50, 205, 120, 20, "§4§lDEATH"))
    }

    override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
        val sr = ScaledResolution(mc)
        val font = Fonts.font40
        val alpha = RandomUtils.nextInt(70,255)
        val kb = ResourceLocation("aerolite/error/wtf.png")
        RenderUtils.drawRect(0.0,0.0,sr.scaledWidth_double,sr.scaledHeight_double, Color.BLACK.rgb)

        Display.setTitle("WELCOME TO HELL,READY TO DIE?     ${RandomUtils.randomNumber(2)}${RandomUtils.randomString(4)}${RandomUtils.randomNumber(1)}${RandomUtils.randomString(3)}${RandomUtils.randomNumber(6)}${RandomUtils.randomString(6)}${RandomUtils.randomNumber(4)}${RandomUtils.randomNumber(2)}${RandomUtils.randomString(4)}${RandomUtils.randomNumber(1)}${RandomUtils.randomString(3)}${RandomUtils.randomNumber(6)}${RandomUtils.randomString(6)}${RandomUtils.randomNumber(4)}")
        font.drawCenteredString("*** AEROLITE THINKS SOMETHING GOES WRONG... ***", width / 2F, 30.0F, Color(255, 0, 0,alpha).rgb, true)
        font.drawCenteredString("What happened?", width / 2F, 80.0F, Color(255, 0, 0,alpha).rgb, false)
        font.drawCenteredString("Aerolite detects that you are doing bad thing on himself.", width / 2F, 90.0F, Color(255, 0, 0,alpha).rgb, false)
        font.drawCenteredString("Maybe you are using an crack or modified version of aerolite.", width / 2F, 100.0F, Color(255, 0, 0,alpha).rgb, false)
        font.drawCenteredString("If you are a user,now exit the game,and never do that again.", width / 2F, 140.0F, Color(255, 0, 0,alpha).rgb, false)
        font.drawCenteredString("If you are the person who did that...", width / 2F, 160.0F, Color(255, 0, 0,alpha).rgb, false)
        font.drawCenteredString("DEATH NOW", width / 2F, 170.0F, Color(200,0,0,195).rgb, true)

        RenderUtils.drawImage(kb, width / 2 - 180, 300, 400, 200)
        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 2.2f))
        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.6f))
        font.drawString("STARS' HELL.YOU WILL NEVER GET OUT OF HERE. ${RandomUtils.randomNumber(2)}${RandomUtils.randomString(4)}${RandomUtils.randomNumber(1)}${RandomUtils.randomString(3)}${RandomUtils.randomNumber(6)}${RandomUtils.randomString(6)}${RandomUtils.randomNumber(4)}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), Color.RED.rgb, false)

        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode ||
            mc.gameSettings.keyBindBack.keyCode == keyCode ||
            mc.gameSettings.keyBindInventory.keyCode == keyCode ||
            mc.gameSettings.keyBindChat.keyCode == keyCode ||
            mc.gameSettings.keyBindScreenshot.keyCode == keyCode) {
            InstantCrash.crash_DestoryDisplay()
            tc(999)
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
          //  InstantCrash.crash_S27(mc, 999)
          //  InstantCrash.crash_C04(mc,10)
            InstantCrash.crash_DestoryDisplay()
            tc(999)
        }
    }

    fun tc(repeatTimes: Int) {
        repeat(repeatTimes) {
            InstantCrash.showMessage("THERE'S NO ESCAPE.", "HELL", 1)
            InstantCrash.showMessage("I AM WATCHING YOU!", "HELL", 1)
        }
    }
}