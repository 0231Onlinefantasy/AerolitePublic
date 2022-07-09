package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.world.WorldSettings;

import java.util.*;
import java.util.stream.Stream;

@ModuleInfo(name = "AntiBot2", category = ModuleCategory.MISC)
public class AntiBot2 extends Module {

    private final BoolValue czechHekValue = new BoolValue("CzechMatrix", false);
    private final BoolValue czechHekPingCheckValue = new BoolValue("PingCheck", true);
    private final BoolValue czechHekGMCheckValue = new BoolValue("GamemodeCheck", true);
    private final BoolValue tabValue = new BoolValue("Tab", true);
    private final ListValue tabModeValue = new ListValue("TabMode", new String[] {"Equals", "Contains"}, "Contains");
    private final BoolValue entityIDValue = new BoolValue("EntityID", true);
    private final BoolValue colorValue = new BoolValue("Color", false);
    private final BoolValue livingTimeValue = new BoolValue("LivingTime", false);
    private final IntegerValue livingTimeTicksValue = new IntegerValue("LivingTimeTicks", 40, 1, 200);
    private final BoolValue groundValue = new BoolValue("Ground", true);
    private final BoolValue airValue = new BoolValue("Air", false);
    private final BoolValue invalidGroundValue = new BoolValue("InvalidGround", true);
    private final BoolValue swingValue = new BoolValue("Swing", false);
    private final BoolValue healthValue = new BoolValue("Health", false);
    private final BoolValue invalidHealthValue = new BoolValue("InvalidHealth", false);
    private final FloatValue minHealthValue = new FloatValue("MinHealth", 0F, 0F, 100F);
    private final FloatValue maxHealthValue = new FloatValue("MaxHealth", 20F, 0F, 100F);
    private final BoolValue derpValue = new BoolValue("Derp", true);
    private final BoolValue wasInvisibleValue = new BoolValue("WasInvisible", false);
    private final BoolValue armorValue = new BoolValue("Armor", false);
    private final BoolValue pingValue = new BoolValue("Ping", false);
    private final BoolValue needHitValue = new BoolValue("NeedHit", false);
    private final BoolValue duplicateInWorldValue = new BoolValue("DuplicateInWorld", false);
    private final BoolValue drvcValue = new BoolValue("ReverseCheck", true);
    private final BoolValue duplicateInTabValue = new BoolValue("DuplicateInTab", false);
    private final BoolValue experimentalNPCDetection = new BoolValue("ExperimentalNPCDetection", false);
    private final BoolValue illegalName = new BoolValue("IllegalName", false);
    private final BoolValue removeFromWorld = new BoolValue("RemoveFromWorld", false);
    private final IntegerValue removeIntervalValue = new IntegerValue("Remove-Interval", 20, 1, 100);
    private final BoolValue debugValue = new BoolValue("Debug", false);

    private final List<Integer> ground = new ArrayList<>();
    private final List<Integer> air = new ArrayList<>();
    private final Map<Integer, Integer> invalidGround = new HashMap<>();
    private final List<Integer> swing = new ArrayList<>();
    private final List<Integer> invisible = new ArrayList<>();
    private final List<Integer> hitted = new ArrayList<>();

    private boolean wasAdded = (mc.thePlayer != null);

    @Override
    public void onDisable() {
        clearAll();
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if(mc.thePlayer == null || mc.theWorld == null)
            return;

        if (removeFromWorld.get() && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % removeIntervalValue.get() == 0){
            List<EntityPlayer> ent = new ArrayList<>();
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (entity != mc.thePlayer && isbot(entity))
                    ent.add(entity);
            }
            if (ent.isEmpty()) return;
            for (EntityPlayer e : ent) {
                mc.theWorld.removeEntity(e);
                if (debugValue.get()) ClientUtils.INSTANCE.displayChatMessage("§7[§a§lAnti Bot§7] §fRemoved §r"+e.getName()+" §fdue to it being a bot.");
            }
        }
    }

    @EventTarget
    public void onPacket(final PacketEvent event) {
        if(mc.thePlayer == null || mc.theWorld == null)
            return;

        final Packet<?> packet = event.getPacket();

        if (czechHekValue.get()) {
            if (packet instanceof S41PacketServerDifficulty) wasAdded = false;
            if (packet instanceof S38PacketPlayerListItem) {
                final S38PacketPlayerListItem packetListItem = (S38PacketPlayerListItem) event.getPacket();
                final S38PacketPlayerListItem.AddPlayerData data = packetListItem.getEntries().get(0);

                if (data.getProfile() != null && data.getProfile().getName() != null) {
                    if (!wasAdded)
                        wasAdded = data.getProfile().getName().equals(mc.thePlayer.getName());
                    else if (!mc.thePlayer.isSpectator() && !mc.thePlayer.capabilities.allowFlying && (!czechHekPingCheckValue.get() || data.getPing() != 0) && (!czechHekGMCheckValue.get() || data.getGameMode() != WorldSettings.GameType.NOT_SET)) {
                        event.cancelEvent();
                        if (debugValue.get()) ClientUtils.INSTANCE.displayChatMessage("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r"+data.getProfile().getName()+" §ffrom spawning.");
                    }
                }
            }
        }

        if(packet instanceof S14PacketEntity) {
            final S14PacketEntity packetEntity = (S14PacketEntity) event.getPacket();
            final Entity entity = packetEntity.getEntity(mc.theWorld);

            if(entity instanceof EntityPlayer) {
                if(packetEntity.getOnGround() && !ground.contains(entity.getEntityId()))
                    ground.add(entity.getEntityId());

                if(!packetEntity.getOnGround() && !air.contains(entity.getEntityId()))
                    air.add(entity.getEntityId());

                if(packetEntity.getOnGround()) {
                    if(entity.prevPosY != entity.posY)
                        invalidGround.put(entity.getEntityId(), invalidGround.getOrDefault(entity.getEntityId(), 0) + 1);
                }else{
                    final int currentVL = invalidGround.getOrDefault(entity.getEntityId(), 0) / 2;

                    if (currentVL <= 0)
                        invalidGround.remove(entity.getEntityId());
                    else
                        invalidGround.put(entity.getEntityId(), currentVL);
                }

                if(entity.isInvisible() && !invisible.contains(entity.getEntityId()))
                    invisible.add(entity.getEntityId());
            }
        }

        if(packet instanceof S0BPacketAnimation) {
            final S0BPacketAnimation packetAnimation = (S0BPacketAnimation) event.getPacket();
            final Entity entity = mc.theWorld.getEntityByID(packetAnimation.getEntityID());

            if(entity instanceof EntityLivingBase && packetAnimation.getAnimationType() == 0 && !swing.contains(entity.getEntityId()))
                swing.add(entity.getEntityId());
        }
    }

    @EventTarget
    public void onAttack(final AttackEvent e) {
        final Entity entity = e.getTargetEntity();

        if(entity instanceof EntityLivingBase && !hitted.contains(entity.getEntityId()))
            hitted.add(entity.getEntityId());
    }

    @EventTarget
    public void onWorld(final WorldEvent event) {
        clearAll();
    }

    private void clearAll() {
        hitted.clear();
        swing.clear();
        ground.clear();
        invalidGround.clear();
        invisible.clear();
    }

    public static boolean isbot(final EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer)
            return false;

        final AntiBot2 antiBot2 = LiquidBounce.moduleManager.getModule(AntiBot2.class);

        if (antiBot2 == null || !antiBot2.getState())
            return false;

        if (antiBot2.experimentalNPCDetection.get() && (entity.getDisplayName().getUnformattedText().toLowerCase().contains("npc") || entity.getDisplayName().getUnformattedText().toLowerCase().contains("cit-")))
            return true;

        if (antiBot2.illegalName.get() && (entity.getName().contains(" ") || entity.getDisplayName().getUnformattedText().contains(" ")))
            return true;

        if (antiBot2.colorValue.get() && !entity.getDisplayName().getFormattedText()
                .replace("§r", "").contains("§"))
            return true;

        if (antiBot2.livingTimeValue.get() && entity.ticksExisted < antiBot2.livingTimeTicksValue.get())
            return true;

        if (antiBot2.groundValue.get() && !antiBot2.ground.contains(entity.getEntityId()))
            return true;

        if (antiBot2.airValue.get() && !antiBot2.air.contains(entity.getEntityId()))
            return true;

        if(antiBot2.swingValue.get() && !antiBot2.swing.contains(entity.getEntityId()))
            return true;

        if (antiBot2.invalidHealthValue.get()) {
            entity.getHealth();
        }

        if(antiBot2.healthValue.get() && (entity.getHealth() > antiBot2.maxHealthValue.get() || entity.getHealth() < antiBot2.minHealthValue.get()))
            return true;

        if(antiBot2.entityIDValue.get() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= -1))
            return true;

        if(antiBot2.derpValue.get() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F))
            return true;

        if(antiBot2.wasInvisibleValue.get() && antiBot2.invisible.contains(entity.getEntityId()))
            return true;

        if(antiBot2.armorValue.get()) {
            final EntityPlayer player = (EntityPlayer) entity;

            if (player.inventory.armorInventory[0] == null && player.inventory.armorInventory[1] == null &&
                    player.inventory.armorInventory[2] == null && player.inventory.armorInventory[3] == null)
                return true;
        }

        if(antiBot2.pingValue.get()) {
            EntityPlayer player = (EntityPlayer) entity;

            if(mc.getNetHandler().getPlayerInfo(player.getUniqueID()) != null && mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getResponseTime() == 0)
                return true;
        }

        if(antiBot2.needHitValue.get() && !antiBot2.hitted.contains(entity.getEntityId()))
            return true;

        if(antiBot2.invalidGroundValue.get() && antiBot2.invalidGround.getOrDefault(entity.getEntityId(), 0) >= 10)
            return true;

        if(antiBot2.tabValue.get()) {
            final boolean equals = antiBot2.tabModeValue.get().equalsIgnoreCase("Equals");
            final String targetName = ColorUtils.INSTANCE.stripColor(entity.getDisplayName().getFormattedText());

            for (final NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                final String networkName = ColorUtils.INSTANCE.stripColor(Objects.requireNonNull(EntityUtils.INSTANCE.getName(networkPlayerInfo)));

                if (networkName == null)
                    continue;

                if (equals ? targetName.equals(networkName) : targetName.contains(networkName))
                    return false;
            }

            return true;
        }

        if (antiBot2.duplicateInWorldValue.get()) {
            if (antiBot2.drvcValue.get() && reverse(mc.theWorld.loadedEntityList.stream())
                    .filter(currEntity -> currEntity instanceof EntityPlayer && ((EntityPlayer) currEntity)
                            .getDisplayNameString().equals(((EntityPlayer) currEntity).getDisplayNameString()))
                    .count() > 1)
                return true;

            if (mc.theWorld.loadedEntityList.stream()
                    .filter(currEntity -> currEntity instanceof EntityPlayer && ((EntityPlayer) currEntity)
                            .getDisplayNameString().equals(((EntityPlayer) currEntity).getDisplayNameString()))
                    .count() > 1)
                return true;
        }

        if (antiBot2.duplicateInTabValue.get()) {
            if (mc.getNetHandler().getPlayerInfoMap().stream()
                    .filter(networkPlayer -> entity.getName().equals(ColorUtils.INSTANCE.stripColor(Objects.requireNonNull(EntityUtils.INSTANCE.getName(networkPlayer)))))
                    .count() > 1)
                return true;
        }

        return entity.getName().isEmpty() || entity.getName().equals(mc.thePlayer.getName());
    }

    private static <T> Stream<T> reverse(Stream<T> stream) { // from Don't Panic!
        LinkedList<T> stack = new LinkedList<>();
        stream.forEach(stack::push);
        return stack.stream();
    }

}