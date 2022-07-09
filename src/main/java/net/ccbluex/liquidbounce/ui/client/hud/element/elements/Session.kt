package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Text.Companion.HOUR_FORMAT
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.FontRenderer
import java.awt.Color


// Session Info Reloaded by Stars
@ElementInfo(name = "Session")
class Session : Element() {
    private val modeValue = ListValue("Mode", arrayOf("Normal", "Hreith"), "Normal")
    private val textredValue = IntegerValue("HreithTextRed", 255, 0, 255)
    private val textgreenValue = IntegerValue("HreithTextGreen", 255, 0, 255)
    private val textblueValue = IntegerValue("HreithTextBlue", 255, 0, 255)
    private val textalphaValue = IntegerValue("HreithTextBlue", 255, 0, 255)
    private val backgroundredValue = IntegerValue("HreithbackgroundRed",0,0,255)
    private val backgroundgreenValue = IntegerValue("HreithbackgroundGreen",0,0,255)
    private val backgroundblueValue = IntegerValue("HreithbackgroundBlue",0,0,255)
    private val bordervalue = BoolValue("HreithSessionBorder", false)

    var s=0;
    var m=0
    var h=0
    var ban=0;
    var staff=0;
    var tag = "Idle..."
    private val bgAlphaValue = IntegerValue("BackgroundAlpha", 160,0,255)
    private val hypixelCheckValue = BoolValue("HypixelCheck", false)

    companion object {
        // no u
        private val API_PUNISHMENT =
            aB("68747470733a2f2f6170692e706c616e636b652e696f2f6879706978656c2f76312f70756e6973686d656e745374617473")
        var WATCHDOG_BAN_LAST_MIN = 0
        var LAST_TOTAL_STAFF = -1
        var STAFF_BAN_LAST_MIN = 0
        var bps = 0
        @JvmStatic
        fun aB(str: String): String { // :trole:
            var result = String()
            val charArray = str.toCharArray()
            var i = 0
            while (i < charArray.size) {
                val st = "" + charArray[i] + "" + charArray[i + 1]
                val ch = st.toInt(16).toChar()
                result += ch
                i += 2
            }
            return result
        }
    }
    override fun drawElement(partialTicks: Float): Border? {
        if (mc.theWorld != null) {

            if (TimerUtils.delay(1000F)) {

                s += 1
                TimerUtils.reset()
            }
            if (s == 60) {
                m += 1;
                s = 0;
            }
            if (m == 60) {
                h += 1;
                m = 0;
            }
        }
        val timer = "${h}h ${m}m ${s}s"
        if (modeValue.get().equals("Normal")) {
            // Code by stars
            RenderUtils.drawRoundedCornerRect(0f + 2, -1f, 180f - 2, 0f, 6F, ColorUtils.rainbow().rgb)
            RenderUtils.drawRoundedCornerRect(0f, 0f, 180f, 105f, 4F, Color(10, 10, 10, bgAlphaValue.get()).rgb)

            Fonts.icon30.drawStringWithShadow("q", 3f, 4f, -1)
            Fonts.font40.drawStringWithShadow("Current Session", 15f, 2.0f, -1)
            Fonts.icon30.drawStringWithShadow("b", 2.5f, 17f, -1)
            Fonts.font35.drawStringWithShadow("Play time", 14.5f, 15.5f, -1)
            Fonts.font35.drawStringWithShadow(
                timer,
                (177F - Fonts.font35.getStringWidth((timer).toString())).toDouble().toFloat(),
                13.5f,
                -1
            )
            Fonts.icon30.drawStringWithShadow("a", 2.5f, 29f, -1)
            Fonts.font35.drawStringWithShadow("Players killed", 14.5f, 28.5f, -1)
            Fonts.font35.drawStringWithShadow(
                LiquidBounce.combatManager.getKillCounts().toString(),
                (177F - Fonts.font35.getStringWidth((LiquidBounce.combatManager.getKillCounts()).toString())).toDouble()
                    .toFloat(), 26.5f, -1
            )
            Fonts.icon30.drawStringWithShadow("e", 2.5f, 43.5f, -1)
            Fonts.font35.drawStringWithShadow("Total Played", 14.5f, 41.5f, -1)
            Fonts.font35.drawStringWithShadow(
                LiquidBounce.combatManager.getTotalPlayed().toString(),
                (177F - Fonts.font35.getStringWidth((LiquidBounce.combatManager.getTotalPlayed()).toString())).toDouble()
                    .toFloat(), 39.5f, -1
            )
            Fonts.icon30.drawStringWithShadow("K", 2.5f, 56f, -1)
            Fonts.font35.drawStringWithShadow("Total staff bans", 14.5f, 54.5f, -1)
            Fonts.font35.drawStringWithShadow(
                LAST_TOTAL_STAFF.toString(),
                (177F - Fonts.font35.getStringWidth((LAST_TOTAL_STAFF).toString())).toDouble().toFloat(),
                52.5f,
                -1
            )
            Fonts.icon30.drawStringWithShadow("L", 2.5f, 69f, -1)
            Fonts.font35.drawStringWithShadow("Total anticheat bans", 14.5f, 67.5f, -1)
            Fonts.font35.drawStringWithShadow(
                WATCHDOG_BAN_LAST_MIN.toString(),
                (177F - Fonts.font35.getStringWidth((WATCHDOG_BAN_LAST_MIN).toString())).toDouble().toFloat(),
                65.5f,
                -1
            )
            Fonts.icon30.drawStringWithShadow("M", 2.5f, 82f, -1)
            Fonts.font35.drawStringWithShadow("Staff Banned", 14.5f, 80.5f, -1)
            Fonts.font35.drawStringWithShadow(
                ban.toString(),
                (177F - Fonts.font35.getStringWidth((ban).toString())).toDouble().toFloat(),
                78.5f,
                -1
            )
            Fonts.icon30.drawStringWithShadow("r", 2.5f, 95f, -1)
            Fonts.font35.drawStringWithShadow("HurtTime", 14.5f, 93.5f, -1)
            Fonts.font35.drawStringWithShadow(
                mc.thePlayer.hurtTime.toString(),
                (177F - Fonts.font35.getStringWidth(mc.thePlayer.hurtTime.toString())).toDouble().toFloat(),
                91.5f,
                -1
            )
        }
        if (modeValue.get().equals("Hreith")) {
            RenderUtils.drawRoundedCornerRect(0.0f,0.0f,189.0f,80.0f,3.0f, Color(backgroundredValue.get(),backgroundgreenValue.get(),backgroundblueValue.get(), bgAlphaValue.get()).rgb)
            Fonts.tc45.drawStringWithShadow("Session Info", 70F, 5F, ColorUtils.rainbow().rgb)
            Fonts.tc45.drawStringWithShadow("PlayerTime                                  ${HOUR_FORMAT.format(System.currentTimeMillis())}",5F,25f,Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
            Fonts.tc45.drawStringWithShadow("HurtTime                                           ${mc.thePlayer.hurtTime}",5F,38f,Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
            Fonts.tc45.drawStringWithShadow("Speed                                                ${bps}",5F,50f,Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
            Fonts.tc45.drawStringWithShadow("PlayerKills                                         ${LiquidBounce.combatManager.getTotalPlayed()}",5F, 64f, Color(textredValue.get(), textgreenValue.get(), textblueValue.get(), 255).rgb)
            if (bordervalue.get()) {
                RenderUtils.drawBorder(0f, 0f, 189f, 80f, 3f, ColorUtils.rainbow().rgb)
            }
        }
        return getBorderSize()
    }

    fun getBorderSize(): Border? {
        return when (modeValue.get()) {
            "Normal" -> Border(0f, -1f, 180f, 105f)
            "Hreith" -> Border(0f,0f,189f,80f)
            else -> null
        }
    }


    init {
        object : Thread("BanCheck-Hypixel") {
            override fun run() {
                val checkTimer = MSTimer()
                while (true) {
                    if (mc.theWorld!=null && hypixelCheckValue.get()) {
                        if (checkTimer.hasTimePassed(800L)) {
                            try {
                                val apiContent = HttpUtils.get(API_PUNISHMENT)
                                val jsonObject = JsonParser().parse(apiContent).asJsonObject
                                if (jsonObject["success"].asBoolean && jsonObject.has("record")) {
                                    val objectAPI = jsonObject["record"].asJsonObject
                                    WATCHDOG_BAN_LAST_MIN = objectAPI["watchdog_total"].asInt
                                    var staffBanTotal = objectAPI["staff_total"].asInt
                                    if (staffBanTotal < LAST_TOTAL_STAFF) staffBanTotal = LAST_TOTAL_STAFF
                                    if (LAST_TOTAL_STAFF == -1) LAST_TOTAL_STAFF = staffBanTotal else {
                                        STAFF_BAN_LAST_MIN = staffBanTotal - LAST_TOTAL_STAFF
                                        LAST_TOTAL_STAFF = staffBanTotal
                                    }
                                    if (STAFF_BAN_LAST_MIN > 1){
                                        ban+=1;
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            checkTimer.reset()
                        }
                    }
                }
            }
        }.start()
    }
}