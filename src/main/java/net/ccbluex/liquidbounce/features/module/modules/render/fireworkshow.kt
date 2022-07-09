package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "fireworkshow", category = ModuleCategory.RENDER)
class fireworkshow : Module() {
//////////////////////////////////
    private val timingValue = ListValue("mode", arrayOf("whendie"), "none")
    private val modeValue = ListValue("firework", arrayOf("firework"), "firework")
    private val timesValue = IntegerValue("Times", 1, 1, 10)
    private val soundfire = BoolValue("firework-sound", false)
    private val blockState = Block.getStateId(Blocks.redstone_block.defaultState)
    ////////////////////////////////
    @EventTarget
    fun onKilled(event: EntityKilledEvent) {
        if(timingValue.equals("whendie")) {
            displayEffectFor(event.targetEntity)
        }
    }
    private fun displayEffectFor(entity: EntityLivingBase) {
        repeat(timesValue.get()) {
            when(modeValue.get().lowercase()) {
                "firework" -> {
                    mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.FIREWORKS_SPARK)
                    if(soundfire.get()) {
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("fireworks.launch"), 1.0f))
                    }
                }
            }
        }
    }
}