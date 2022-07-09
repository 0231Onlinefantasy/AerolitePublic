/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

/**
 * Called when player attacks other entity
 *
 * @param targetEntity Attacked entity
 */
class AttackEvent(val targetEntity: Entity) : CancellableEvent()

/**
 * Called when player killed other entity
 *
 * @param targetEntity Attacked entity
 */
class EntityKilledEvent(val targetEntity: EntityLivingBase) : Event()

/**
 * Called when minecraft get bounding box of block
 *
 * @param blockPos block position of block
 * @param block block itself
 * @param boundingBox vanilla bounding box
 */
class BlockBBEvent(blockPos: BlockPos, val block: Block, var boundingBox: AxisAlignedBB?) : Event() {
    val x = blockPos.x
    val y = blockPos.y
    val z = blockPos.z
}


/**
 * Called when player clicks a block
 */
class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

/**
 * Called when client is shutting down
 */
class ClientShutdownEvent : Event()

class PreUpdateEvent(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var isSneaking: Boolean, var isOnGround: Boolean) : Event() {

    fun fire() {
        prevYAW = YAW
        prevPITCH = PITCH
        YAW = yaw
        PITCH = pitch
        SNEAKING = isSneaking
        OnGround = isOnGround
    }

    fun getYaw(yaw: Float): Float {
        return yaw
    }

    fun getPitch(pitch: Float): Float {
        return pitch
    }

    companion object {
        var YAW = 0f
        var PITCH = 0f
        var prevYAW = 0f
        var prevPITCH = 0f
        var SNEAKING = false
        var OnGround = false
    }
}

/**
 * Called when player jumps
 *
 * @param motion jump motion (y motion)
 */
class JumpEvent(var motion: Float) : CancellableEvent()

/**
 * Called when user press a key once
 *
 * @param key Pressed key
 */
class KeyEvent(val key: Int) : Event()

/**
 * Called in "onUpdateWalkingPlayer"
 *
 * @param eventState PRE or POST
 */
//class MotionEvent(val eventState: EventState) : Event()

class MotionEvent(var eventstate: EventState, var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var onGround: Boolean) : Event() {
    var eventState: EventState = eventstate
}
/**
 * Called in "onLivingUpdate" when the player is using a use item.
 *
 * @param strafe the applied strafe slow down
 * @param forward the applied forward slow down
 */
class SlowDownEvent(var strafe: Float, var forward: Float) : Event()

/**
 * Called in "moveFlying"
 */
class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : CancellableEvent()

/**
 * Called when player moves
 *
 * @param x motion
 * @param y motion
 * @param z motion
 */
class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {
    var isSafeWalk = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

/**
 * Called when receive or send a packet
 */
class PacketEvent(val packet: Packet<*>, val type: Type) : CancellableEvent() {
    enum class Type {
        RECEIVE,
        SEND
    }

    @JvmName("getPacket1")
    fun getPacket(): Packet<*> {
        return packet
    }

    fun setCancelled(aaa: Boolean) {
        cancelEvent()
    }

    fun isServerSide() = type == Type.RECEIVE
}

/**
 * Called when a block tries to push you
 */
class PushOutEvent : CancellableEvent()

/**
 * Called when screen is going to be rendered
 */
class Render2DEvent(val partialTicks: Float, val scaledResolution: ScaledResolution) : Event()

/**
 * Called when world is going to be rendered
 */
class Render3DEvent(val partialTicks: Float) : Event()

/**
 * Called when entity is going to be rendered
 */
class RenderEntityEvent(
    val entity: Entity,
    val x: Double,
    val y: Double,
    val z: Double,
    val entityYaw: Float,
    val partialTicks: Float
) : Event()

/**
 * Called when the screen changes
 */
class ScreenEvent(val guiScreen: GuiScreen?) : Event()

/**
 * Called when player is going to step
 */
class StepEvent(var stepHeight: Float, val eventState: EventState) : Event()

/**
 * Called when a text is going to be rendered
 */
class TextEvent(var text: String?) : Event()

/**
 * tick... tack... tick... tack
 */
class TickEvent : Event()

/**
 * Called when minecraft player will be updated
 */
class UpdateEvent : Event()

/**
 * Called when the world changes
 */
class WorldEvent(var worldClient: WorldClient?) : Event()

/**
 * Called when window clicked
 */
class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) : CancellableEvent()

/**
 * Called when update da model
 */
class UpdateModelEvent(val player: EntityPlayer, val model: ModelPlayer) : Event()
