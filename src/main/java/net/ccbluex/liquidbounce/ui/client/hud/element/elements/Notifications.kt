package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", blur = true)
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)
    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        val notifications = mutableListOf<Notification>()
        //FUCK YOU java.util.ConcurrentModificationException
        for ((index, notify) in LiquidBounce.hud.notifications.withIndex()) {
            GL11.glPushMatrix()

            if (notify.drawNotification(index)) {
                notifications.add(notify)
            }

            GL11.glPopMatrix()
        }
        for (notify in notifications) {
            LiquidBounce.hud.notifications.remove(notify)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }

}

class Notification(val title: String, val content: String, val type: NotifyType, val time: Int = 800, val animeTime: Int = 500) {
    val width = 140.coerceAtLeast(
        Fonts.minecraftFont.getStringWidth(this.title)
            .coerceAtLeast(Fonts.minecraftFont.getStringWidth(content)) + 10
    )
    val height = 50

    var fadeState = FadeState.IN
    var nowY = -height
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()


    /**
     * Draw notification
     */
    fun drawNotification(index: Int): Boolean {
        val realY = -(index + 1) * (height + 10)
        val nowTime = System.currentTimeMillis()

        val pn = ResourceLocation(
            when (type.name) {
                "SUCCESS" -> "aerolite/noti/SUCCESS.png"
                "ERROR" -> "aerolite/noti/ERROR.png"
                "WARNING" -> "aerolite/noti/WARNING.png"
                "INFO" -> "aerolite/noti/INFO.png"
                else -> "aerolite/error/error1.png"
            }
        )

        //Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = EaseUtils.easeOutExpo(pct)
            }
            GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
        } else {
            animeYTime = nowTime
        }
        GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

        //X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = EaseUtils.easeOutExpo(pct)
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct > 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = 1 - EaseUtils.easeInExpo(pct)
            }

            FadeState.END -> {
                return true
            }
        }
                GL11.glScaled(pct, pct, pct)
                GL11.glTranslatef(-width.toFloat(), 0F, 0F)

                var fontcolor = 0
                if (type.toString() == "SUCCESS") {
                    fontcolor = Color(40, 250, 40, 75).rgb
                }
                if (type.toString() == "ERROR") {
                    fontcolor = Color(250, 40, 40, 75).rgb
                }
                if (type.toString() == "WARNING") {
                    fontcolor = Color(219, 167, 20, 75).rgb
                }
                if (type.toString() == "INFO") {
                    fontcolor = Color(106, 106, 245, 75).rgb
                }
                RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), Color(63, 63, 63, 210))
                RenderUtils.drawGradientSidewaysV(
                    0.0,
                    height.toDouble(),
                    width.toDouble(),
                    height.toDouble() + 2,
                    Color(1, 1, 1, 15).rgb,
                    Color(1, 1, 1, 0).rgb
                )
                RenderUtils.drawGradientSidewaysV(
                    0.0,
                    0.0,
                    width.toDouble(),
                    0.0 - 2,
                    Color(1, 1, 1, 15).rgb,
                    Color(1, 1, 1, 0).rgb
                )
                RenderUtils.drawRect(
                    0.0f,
                    0F,
                    width * ((nowTime - displayTime) / (animeTime * 2F + time)),
                    height.toFloat(),
                    fontcolor
                )
                Fonts.poppinsBold20.drawString(title, 35f, 15.5f, Color.WHITE.rgb)
                Fonts.font35.drawString(content, 35f, 28f, Color.gray.rgb)
                RenderUtils.drawFilledCircle(width - 7, 6, 2F, Color(255, 255, 255, 220))
                RenderUtils.drawImage(pn, 8, 18, 17, 17)
                GlStateManager.resetColor()


                return false
            }
        }


enum class NotifyType(var icon: String) {
    SUCCESS("check-circle"),
    ERROR("close-circle"),
    WARNING("warning"),
    INFO("information");
}


enum class FadeState { IN, STAY, OUT, END }

