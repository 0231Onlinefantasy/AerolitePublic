package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "Aura", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
    private val maxCpsValue: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCpsValue.get()
            if (i > newValue) set(i)

            attackDelay = getAttackDelay(minCpsValue.get(), this.get())
        }
    }

    private val minCpsValue: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCpsValue.get()
            if (i < newValue) set(i)

            attackDelay = getAttackDelay(this.get(), maxCpsValue.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val combatDelayValue = BoolValue("1.9Combat", false)

    // Range
    val rangeValue = object : FloatValue("Range", 3.7f, 1f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }
    private val throughWallsRangeValue = object : FloatValue("ThroughWallsRange", 1.5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = rangeValue.get()
            if (i < newValue) set(i)
        }
    }
    private val swingRangeValue = object : FloatValue("SwingRange", 5f, 0f, 15f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
            if (maxRange > newValue) set(maxRange)
        }
    }
    private val discoverRangeValue = FloatValue("DiscoverRange", 6f, 0f, 15f)

    // Modes
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Fov", "LivingTime", "Armor", "HurtResistantTime"), "Distance")
    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Single")

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val attackTimingValue = ListValue("AttackTiming", arrayOf("All", "Pre", "Post", "Both"), "All")
    private val keepSprintValue = BoolValue("KeepSprint", true)

    // AutoBlock
    val autoBlockValue = ListValue("AutoBlock", arrayOf("Range", "Fake", "Off", "NCP", "Vulcan", "Hypixel"), "Off")
    // vanilla will send block packet at pre
    private val blockTimingValue = ListValue("BlockTiming", arrayOf("Pre", "Post", "Both"), "Both").displayable { autoBlockValue.equals("Range") }
    private val autoBlockRangeValue = object : FloatValue("AutoBlockRange", 2.5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { autoBlockValue.equals("Range") }
    private val autoBlockPacketValue = ListValue("AutoBlockPacket", arrayOf("AfterTick", "AfterAttack", "Vanilla"), "AfterTick").displayable { autoBlockValue.equals("Range") }
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true).displayable { autoBlockValue.equals("Range") }
    private val blockRateValue = IntegerValue("BlockRate", 100, 1, 100).displayable { autoBlockValue.equals("Range") }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false).displayable { raycastValue.get() }
    private val livingRaycastValue = BoolValue("LivingRayCast", true).displayable { raycastValue.get() }

    // Bypass
    private val aacValue = BoolValue("AAC", true)
    // TODO: Divide AAC Opinion into three separated opinions

    // Rotations
    private val rotationModeValue = ListValue("RotationMode", arrayOf("None", "LiquidBounce", "ForceCenter", "SmoothCenter", "SmoothLiquid", "LockView", "Hypixel", "Hypixel2", "Exhibition", "BackTrack"), "LiquidBounce")
    // TODO: RotationMode Bypass Intave

    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }

    private val rotationSmoothModeValue = ListValue("SmoothMode", arrayOf("Custom", "Line", "Quad", "Sine", "QuadSine"), "Custom")

    private val rotationSmoothValue = FloatValue("CustomSmooth", 2f, 1f, 10f).displayable { rotationSmoothModeValue.equals("Custom") }

    private val randomCenterModeValue = ListValue("RandomCenter", arrayOf("Off", "Cubic", "Horizonal", "Vertical"), "Off")
    private val randomCenRangeValue = FloatValue("RandomRange", 0.0f, 0.0f, 1.2f)

    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotationModeValue.equals("None") }
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Silent").displayable { silentRotationValue.get() && !rotationModeValue.equals("None") }
    private val strafeOnlyGroundValue = BoolValue("StrafeOnlyGround", true).displayable { rotationStrafeValue.displayable && !rotationStrafeValue.equals("Off") }
    private val rotationRevValue = BoolValue("RotationReverse", false).displayable { !rotationModeValue.equals("None") }
    private val rotationRevTickValue = IntegerValue("RotationReverseTick", 5, 1, 20).displayable { !rotationModeValue.equals("None") }
    private val keepDirectionValue = BoolValue("KeepDirection", true).displayable { !rotationModeValue.equals("None") }
    private val keepDirectionTickValue = IntegerValue("KeepDirectionTick", 15, 1, 20).displayable { !rotationModeValue.equals("None") }
    private val hitableValue = BoolValue("AlwaysHitable", true).displayable { !rotationModeValue.equals("None") }
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val boundingBoxModeValue = ListValue("LockLocation", arrayOf("Head","Auto","Default"), "Auto").displayable { !rotationModeValue.equals("None")}
    private val predictValue = BoolValue("Predict", true).displayable { !rotationModeValue.equals("None") }

    private val maxPredictSizeValue: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    private val minPredictSizeValue: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true).displayable { failRateValue.get() != 0f }
    private val noInventoryAttackValue = ListValue("NoInvAttack", arrayOf("Spoof", "CancelRun", "Off"), "Off")

    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500)
    private val switchDelayValue = IntegerValue("SwitchDelay", 300, 1, 2000).displayable { targetModeValue.equals("Switch") }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.equals("Multi") }

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid", "Jello", "Matrix", "Vape", "None"), "Vape")
    private val matrixRadiusValue = FloatValue("MatrixRadius", 2f, 0.1f, 4f)
    private val circleValue = BoolValue("Circle", false)
    private val circleRedValue = IntegerValue("CircleR", 255, 0, 255).displayable { circleValue.get() }
    private val circleGreenValue = IntegerValue("CircleG", 255, 0, 255).displayable { circleValue.get() }
    private val circleBlueValue = IntegerValue("CircleB", 255, 0, 255).displayable { circleValue.get() }
    private val circleAlphaValue = IntegerValue("CircleAlpha", 255, 0, 255).displayable { circleValue.get() }
    private val circleThicknessValue = FloatValue("CircleSize", 2F, 1F, 5F).displayable { circleValue.get() }

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Swing
    private val swingTimer = MSTimer()
    private var swingDelay = 0L
    private var canSwing = false

    // Fake block status
    var blockingStatus = false

    val displayBlocking: Boolean
        get() = blockingStatus || (autoBlockValue.equals("Fake") && canFakeBlock)

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        discoveredTargets.clear()
        inRangeDiscoveredTargets.clear()
        attackTimer.reset()
        clicks = 0
        canSwing = false
        swingTimer.reset()

        stopBlocking()
        RotationUtils.setTargetRotationReverse(RotationUtils.serverRotation, 0, 0)
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isRiding) {
            return
        }

        if (attackTimingValue.equals("All") || attackTimingValue.equals("Both") ||
            (attackTimingValue.equals("Pre") && event.eventState == EventState.PRE) ||
            (attackTimingValue.equals("Post") && event.eventState == EventState.POST)) {
            runAttackLoop()
        }
        val target = this.target ?: discoveredTargets.first()
        if (blockTimingValue.equals("Both") ||
            (blockTimingValue.equals("Pre") && event.eventState == EventState.PRE) ||
            (blockTimingValue.equals("Post") && event.eventState == EventState.POST)) {
            // AutoBlock
            if ((autoBlockValue.equals("Range") || autoBlockValue.equals("NCP")) && discoveredTargets.isNotEmpty() && (!autoBlockPacketValue.equals("AfterAttack")
                        || discoveredTargets.any { mc.thePlayer.getDistanceToEntityBox(it) > maxRange }) && canBlock) {
                if (mc.thePlayer.getDistanceToEntityBox(target) < autoBlockRangeValue.get()) {
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                }
            }

        }
        if (event.eventState !== EventState.PRE && autoBlockValue.get().equals("Vulcan", true) && canBlock) {
            startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
        }
        if (target == null) {
            stopBlocking()
        }

        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            return
        }

        if (rotationStrafeValue.equals("Off")) {
            update()
        }
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.equals("Off") && !mc.thePlayer.isRiding) {
            return
        }

        // if(event.eventState == EventState.PRE)
        update()

        if (strafeOnlyGroundValue.get() && !mc.thePlayer.onGround) {
            return
        }

        // TODO: Fix Rotation issue on Strafe POST Event

        if (discoveredTargets.isNotEmpty() && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().lowercase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F) {
                            f = 1.0F
                        }

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
                "silent" -> {
                    // update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
            }
        }
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))) {
            return
        }

        // Update target
        updateTarget()

        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.equals("Switch") && (currentTarget != null && EntityUtils.isSelected(currentTarget!!, true))) {
            target = currentTarget
        }
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate() {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            return
        }

        if(autoBlockValue.equals("Hypixel")){
            if(mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword){
                if (mc.thePlayer.swingProgressInt === -1) {
                    PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                } else if (mc.thePlayer.swingProgressInt < 0.5 && mc.thePlayer.swingProgressInt !== -1) {
                    PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.heldItem, 0F, 0F, 0F))
                    //mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, new Vec3((double)MathUtil.getRandomFloat(-50, 50)/100, (double)MathUtil.getRandomFloat(0, 200)/100, (double)MathUtil.getRandomFloat(-50, 50)/100)));
                    //mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT)); mc.playerController.syncCurrentPlayItem();
                    mc.thePlayer.sendQueue.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                }
            }
        }
        if(target == null && autoBlockValue.get().equals("Vulcan", true)){
            stopBlocking()
        }


        if (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (!rotationStrafeValue.equals("Off") && !mc.thePlayer.isRiding) {
            return
        }

        if (mc.thePlayer.isRiding) {
            update()
        }

        if (attackTimingValue.equals("All")) {
            runAttackLoop()
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(circleThicknessValue.get())
            GL11.glColor4f(circleRedValue.get().toFloat() / 255.0F, circleGreenValue.get().toFloat() / 255.0F, circleBlueValue.get().toFloat() / 255.0F, circleAlphaValue.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 5) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
        }
        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) && currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = getAttackDelay(minCpsValue.get(), maxCpsValue.get())
        }

        discoveredTargets.forEach {
            when (markValue.get().lowercase()) {
                "liquid" -> {
                    RenderUtils.drawPlatform(it, if (it.hurtTime <= 0) Color(37, 126, 255, 170) else Color(255, 0, 0, 170))
                }
                "vape" -> {
                    val bb = it.entityBoundingBox
                    it.entityBoundingBox = bb.expand(0.2, 0.2, 0.2)
                    if (LiquidBounce.combatManager.inCombat) RenderUtils.drawEntityBox(it, if (it.hurtTime <= 0) if (it == target) Color.RED else Color.RED else Color.RED, true, true, 1f)
                    else RenderUtils.drawEntityBox(it, if (it.hurtTime <= 0) if (it == target) Color.PINK else Color.PINK else Color.PINK, true, true, 1f)
                    it.entityBoundingBox = bb
                }
                "jello" -> {
                    val everyTime = 3000
                    val drawTime = (System.currentTimeMillis() % everyTime).toInt()
                    val drawMode = drawTime > (everyTime/2)
                    var drawPercent = drawTime / (everyTime/2.0)
                    // true when goes up
                    if (!drawMode) {
                        drawPercent = 1 - drawPercent
                    } else {
                        drawPercent -= 1
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7425)
                    mc.entityRenderer.disableLightmap()

                    val bb = it.entityBoundingBox
                    val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
                    val height = bb.maxY - bb.minY
                    val x = it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y = (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
                    val z = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    val eased = (height / 3) * (if (drawPercent > 0.5) { 1 - drawPercent } else { drawPercent }) * (if (drawMode) { -1 } else { 1 })
                    for (i in 5..360 step 5) {
                        val color = Color(255,255,255).rgb
                        val x1 = x - sin(i * Math.PI / 180F) * radius
                        val z1 = z + cos(i * Math.PI / 180F) * radius
                        val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
                        val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
                        GL11.glBegin(GL11.GL_QUADS)
                        RenderUtils.glColor(color, 0f)
                        GL11.glVertex3d(x1, y + eased, z1)
                        GL11.glVertex3d(x2, y + eased, z2)
                        RenderUtils.glColor(color, 150f)
                        GL11.glVertex3d(x2, y, z2)
                        GL11.glVertex3d(x1, y, z1)
                        GL11.glEnd()
                    }

                    GL11.glEnable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7424)
                    GL11.glColor4f(1f, 1f, 1f, 1f)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }
                "matrix" -> {
                    GL11.glDisable(3553)
                    GL11.glEnable(2848)
                    GL11.glEnable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3042)
                    GL11.glBlendFunc(770, 771)
                    GL11.glHint(3154, 4354)
                    GL11.glHint(3155, 4354)
                    GL11.glHint(3153, 4354)
                    GL11.glDisable(2929)
                    GL11.glDepthMask(false)
                    GL11.glLineWidth(3f)
                    val target = LiquidBounce.combatManager.target
                    GL11.glBegin(3)
                    val x = target!!.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                    val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                    val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                    val radius = matrixRadiusValue.get()
                    for (i in 0..360 step 30) {
                        RenderUtils.glColor(Color.getHSBColor(if (i < 180) { HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (i / 180f) } else { HUD.rainbowStartValue.get() + (HUD.rainbowStopValue.get() - HUD.rainbowStartValue.get()) * (-(i-360) / 180f) }, 0.7f, 1.0f))
                        GL11.glVertex3d(x - sin(i * Math.PI / 180F) * radius, y, z + cos(i * Math.PI / 180F) * radius)
                    }
                    GL11.glEnd()

                    GL11.glDepthMask(true)
                    GL11.glEnable(2929)
                    GL11.glDisable(2848)
                    GL11.glDisable(2881)
                    GL11.glEnable(2832)
                    GL11.glEnable(3553)
                }
            }
        }
    }

    /**
     * Handle entity move
     */
//    @EventTarget
//    fun onEntityMove(event: EntityMovementEvent) {
//        val movedEntity = event.movedEntity
//
//        if (target == null || movedEntity != currentTarget)
//            return
//
//        updateHitable()
//    }

    private fun runAttackLoop() {
        if (clicks <= 0 && canSwing && swingTimer.hasTimePassed(swingDelay)) {
            swingTimer.reset()
            swingDelay = getAttackDelay(minCpsValue.get(), maxCpsValue.get())
            runSwing()
            return
        }

        while (clicks > 0) {
            runAttack()
            clicks--
        }
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val openInventory = noInventoryAttackValue.equals("Spoof") && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Check is not hitable or check failrate
        if (hitable && !failHit) {
            // Close inventory when open
            if (openInventory) {
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }

            // Attack
            if (!targetModeValue.equals("Multi")) {
                attackEntity(currentTarget!!)
            } else {
                inRangeDiscoveredTargets.forEachIndexed { index, entity ->
                    if (limitedMultiTargetsValue.get() == 0 || index <limitedMultiTargetsValue.get()) {
                        attackEntity(entity)
                    }
                }
            }

            if (targetModeValue.equals("Switch")) {
                if (switchTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                    prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                    switchTimer.reset()
                }
            } else {
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
            }

            if (target == currentTarget) {
                target = null
            }

            // Open inventory
            if (openInventory) {
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            }
        } else if (fakeSwingValue.get()) {
            runSwing()
        }
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, true) || (switchMode && prevTargetEntities.contains(entity.entityId))) {
                continue
            }

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= discoverRangeValue.get() && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime) {
                discoveredTargets.add(entity)
            }
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health } // Sort by health
            "fov" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> discoveredTargets.sortBy { it.totalArmorValue } // Sort by armor
            "hurtresistanttime" -> discoveredTargets.sortBy { it.hurtResistantTime } // Sort by armor
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) <getRange(it) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty() && prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        // Find best target
        for (entity in discoveredTargets) {
            // Update rotations to current target
            if (!updateRotations(entity)) { // when failed then try another target
                continue
            }

            // Set target to current entity
            if (mc.thePlayer.getDistanceToEntityBox(entity) < maxRange) {
                target = entity
                canSwing = false
                return
            }
        }

        target = null
        canSwing = discoveredTargets.find { mc.thePlayer.getDistanceToEntityBox(it) < swingRangeValue.get() } != null
    }

    private fun runSwing() {
        val swing = swingValue.get()
        if (swing.equals("packet", true)) {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        } else if (swing.equals("normal", true)) {
            mc.thePlayer.swingItem()
        }
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {
        // Call attack event
        val event = AttackEvent(entity)
        LiquidBounce.eventManager.callEvent(event)
        if (event.isCancelled) {
            return
        }

        // Stop blocking
        if (!autoBlockPacketValue.equals("Vanilla") && (mc.thePlayer.isBlocking || blockingStatus)) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }

        // Attack target
        runSwing()

        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (keepSprintValue.get()) {
            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F) {
                mc.thePlayer.onEnchantmentCritical(entity)
            }
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
            }
        }

        // Start blocking after attack
        if (mc.thePlayer.isBlocking || (autoBlockValue.equals("Range") && canBlock)) {
            if (autoBlockPacketValue.equals("AfterTick")) {
                return
            }

            if (!(blockRateValue.get() > 0 && Random().nextInt(100) <= blockRateValue.get())) {
                return
            }

            startBlocking(entity, interactAutoBlockValue.get())
        }

        if (!blockingStatus && autoBlockValue.equals("Verus")) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotationModeValue.equals("None")) {
            return true
        }
        val bb = entity.entityBoundingBox
        val predictSize = if(predictValue.get()) floatArrayOf(minPredictSizeValue.get(),maxPredictSizeValue.get()) else floatArrayOf(0.0F,0.0F)
        val predict = doubleArrayOf(
            (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(predictSize[0], predictSize[1]),
            (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(predictSize[0], predictSize[1]),
            (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(predictSize[0], predictSize[1]))
        var boundingBox = when(boundingBoxModeValue.get()) {
            "Head" -> AxisAlignedBB(max(bb.minX,bb.minX + predict[0]),max(bb.minY,bb.minY + predictSize[1]),max(bb.minZ,bb.minZ + predict[2]),min(bb.maxX,bb.maxX + predict[0]),min(bb.maxY,bb.maxY + predictSize[1]),min(bb.maxZ,bb.maxZ + predict[2]))
            "Auto" -> bb.offset(predict[0],predict[1],predict[2])
            else -> entity.entityBoundingBox
        }
        if (predictValue.get()) {
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSizeValue.get(), maxPredictSizeValue.get()),
                (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSizeValue.get(), maxPredictSizeValue.get()),
                (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSizeValue.get(), maxPredictSizeValue.get())
            )
        }
        val rModes = when (rotationModeValue.get()) {
            "LiquidBounce", "SmoothLiquid" -> "LiquidBounce"
            "ForceCenter", "SmoothCenter" -> "CenterLine"
            "LockView" -> "CenterSimple"
            else -> "LiquidBounce"
        }

        val (_, directRotation) =
            RotationUtils.calculateCenter(
                rModes,
                randomCenterModeValue.get(),
                (randomCenRangeValue.get()).toDouble(),
                boundingBox,
                predictValue.get() && rotationModeValue.get() != "Test",
                mc.thePlayer.getDistanceToEntityBox(entity) <= throughWallsRangeValue.get()
            ) ?: return false

        if (rotationModeValue.get() == "OldMatrix") directRotation.pitch = (89.9).toFloat()


        var diffAngle = RotationUtils.getRotationDifference(RotationUtils.serverRotation, directRotation)
        if (diffAngle <0) diffAngle = -diffAngle
        if (diffAngle> 180.0) diffAngle = 180.0

        val calculateSpeed = when (rotationSmoothModeValue.get()) {
            "Custom" -> diffAngle / rotationSmoothValue.get()
            "Line" -> (diffAngle / 180) * maxTurnSpeedValue.get() + (1 - diffAngle / 180) * minTurnSpeedValue.get()
            //"Quad" -> Math.pow((diffAngle / 180.0), 2.0) * maxTurnSpeedValue.get() + (1 - Math.pow((diffAngle / 180.0), 2.0)) * minTurnSpeedValue.get()
            "Quad" -> (diffAngle / 180.0).pow(2.0) * maxTurnSpeedValue.get() + (1 - (diffAngle / 180.0).pow(2.0)) * minTurnSpeedValue.get()
            "Sine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5) * maxTurnSpeedValue.get() + (cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5) * minTurnSpeedValue.get()
            //"QuadSine" -> Math.pow(-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5, 2.0) * maxTurnSpeedValue.get() + (1 - Math.pow(-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5, 2.0)) * minTurnSpeedValue.get()
            "QuadSine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0) * maxTurnSpeedValue.get() + (1 - (-cos(
                diffAngle / 180 * Math.PI
            ) * 0.5 + 0.5).pow(2.0)) * minTurnSpeedValue.get()
            else -> 180.0
        }

        fun getHypixelRotations(vec: Vec3): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            val yaw = (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-Math.atan2(diffY, dist) * 180.0 / 3.141592653589793).toFloat()
            return Rotation(yaw, pitch)
        }
        fun getNCPRotations(vec: Vec3, predict: Boolean): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            val hypotenuse = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            return Rotation(
                (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f,
                (-Math.atan2(diffY, hypotenuse) * 180.0 / 3.141592653589793).toFloat())
        }

        val rotation = when (rotationModeValue.get()) {
            "LiquidBounce", "ForceCenter" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation, directRotation,
                (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat())
            "LockView" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation, directRotation, (180.0).toFloat())
            "SmoothCenter", "SmoothLiquid", "OldMatrix" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation, directRotation, (calculateSpeed).toFloat())
            "Hypixel" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat())
            "Hypixel2" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation, getHypixelRotations(RotationUtils.getCenter(entity.entityBoundingBox)),(Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat())
            "Exhibition" ->RotationUtils.limitAngleChange(RotationUtils.serverRotation, getNCPRotations(RotationUtils.getCenter(entity.entityBoundingBox),false),(Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat())
            "BackTrack" -> RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                RotationUtils.otherRotation(boundingBox,RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(),
                    mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),maxRange), (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat())
            else -> return true
        }


        if (silentRotationValue.get()) {
            if (rotationRevTickValue.get()> 0 && rotationRevValue.get()) {
                if (keepDirectionValue.get()) {
                    RotationUtils.setTargetRotationReverse(rotation, keepDirectionTickValue.get(), rotationRevTickValue.get())
                } else {
                    RotationUtils.setTargetRotationReverse(rotation, 1, rotationRevTickValue.get())
                }
            } else {
                if (keepDirectionValue.get()) {
                    RotationUtils.setTargetRotation(rotation, keepDirectionTickValue.get())
                } else {
                    RotationUtils.setTargetRotation(rotation, 1)
                }
            }
        } else {
            rotation.toPlayer(mc.thePlayer)
        }
        return true
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (hitableValue.get()) {
            hitable = true
            return
        }
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeedValue.get() <= 0F) {
            hitable = true
            return
        }

        val reach = maxRange.toDouble()

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach) {
                (!livingRaycastValue.get() || it is EntityLivingBase && it !is EntityArmorStand) &&
                        (EntityUtils.isSelected(it, true) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld.getEntitiesWithinAABBExcludingEntity(it, it.entityBoundingBox).isNotEmpty())
            }

            if (raycastValue.get() && raycastedEntity is EntityLivingBase &&
                !EntityUtils.isFriend(raycastedEntity)) {
                currentTarget = raycastedEntity
            }

            hitable = if (!rotationModeValue.equals("None")) currentTarget == raycastedEntity else true
        } else {
            hitable = RotationUtils.isFaced(currentTarget, reach)
        }
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockValue.equals("Range") && mc.thePlayer.getDistanceToEntityBox(interactEntity)> autoBlockRangeValue.get()) {
            return
        }

        if (autoBlockValue.equals("NCP")) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
            blockingStatus = true
            return
        }

        if (blockingStatus) {
            return
        }

        if (interact) {
            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, interactEntity.positionVector))
            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, if (MovementUtils.isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }

    /**
     * Attack Delay
     */
    private fun getAttackDelay(minCps: Int, maxCps: Int): Long {
        var delay = TimeUtils.randomClickDelay(minCps.coerceAtMost(maxCps), minCps.coerceAtLeast(maxCps))
        if (combatDelayValue.get()) {
            var value = 4.0
            if (mc.thePlayer.inventory.getCurrentItem() != null) {
                when (mc.thePlayer.inventory.getCurrentItem().item) {
                    is ItemSword -> {
                        value -= 2.4
                    }
                    is ItemPickaxe -> {
                        value -= 2.8
                    }
                    is ItemAxe -> {
                        value -= 3
                    }
                }
            }
            delay = delay.coerceAtLeast((1000 / value).toLong())
        }
        return delay
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 3

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    private fun getRange(entity: Entity) =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) rangeValue.get() else throughWallsRangeValue.get())

    /**
     * HUD Tag
     */
    override val tag: String
        get() = "${targetModeValue.get()}Mode"
}
//1