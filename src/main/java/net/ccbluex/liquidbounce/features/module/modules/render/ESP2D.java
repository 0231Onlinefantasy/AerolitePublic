//114514
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.BlendUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@ModuleInfo(name = "ESP2D", category = ModuleCategory.RENDER)
public final class ESP2D extends Module {
   public final BoolValue outline = new BoolValue("Outline", true);
   public final ListValue boxMode = new ListValue("Mode", new String[]{"Box", "Corners"}, "Box");
   public final BoolValue healthBar = new BoolValue("Health-bar", true);
   public final BoolValue armorBar = new BoolValue("Armor-bar", true);
   public final BoolValue details = new BoolValue("Details", true);
   public final BoolValue tagsValue = new BoolValue("Tags", true);
   public final BoolValue itemTagsValue = new BoolValue("Item-Tags", true);
   public final BoolValue clearNameValue = new BoolValue("Use-Clear-Name", false);
   public final BoolValue absorption = new BoolValue("Render-Absorption", true);
   public final BoolValue localPlayer = new BoolValue("Local-Player", true);
   public final BoolValue droppedItems = new BoolValue("Dropped-Items", false);
   private final ListValue colorModeValue = new ListValue("Color", new String[] {"Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade"}, "Custom");
	private final IntegerValue colorRedValue = new IntegerValue("Red", 255, 0, 255);
	private final IntegerValue colorGreenValue = new IntegerValue("Green", 255, 0, 255);
	private final IntegerValue colorBlueValue = new IntegerValue("Blue", 255, 0, 255);
	private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
	private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
	private final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10);
   private final BoolValue colorTeam = new BoolValue("Team", false);
   public static List collectedEntities = new ArrayList();
   private final IntBuffer viewport;
   private final FloatBuffer modelview;
   private final FloatBuffer projection;
   private final FloatBuffer vector;
   private final int backgroundColor;
   private final int black;

   private final DecimalFormat dFormat = new DecimalFormat("0.0");

   public ESP2D() {
      this.viewport = GLAllocation.createDirectIntBuffer(16);
      this.modelview = GLAllocation.createDirectFloatBuffer(16);
      this.projection = GLAllocation.createDirectFloatBuffer(16);
      this.vector = GLAllocation.createDirectFloatBuffer(4);
      this.backgroundColor = new Color(0, 0, 0, 120).getRGB();
      this.black = Color.BLACK.getRGB();
   }

   public final Color getColor(final Entity entity) {
		if (entity instanceof EntityLivingBase) {
			final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

			if (entityLivingBase.hurtTime > 0)
				return Color.RED;

			if (EntityUtils.INSTANCE.isFriend(entityLivingBase))
				return Color.BLUE;

			if (colorTeam.get()) {
				final char[] chars = entityLivingBase.getDisplayName().getFormattedText().toCharArray();
				int color = Integer.MAX_VALUE;

				for (int i = 0; i < chars.length; i++) {
					if (chars[i] != '§' || i + 1 >= chars.length)
						continue;

					final int index = GameFontRenderer.Companion.getColorIndex2(chars[i + 1]);

					if (index < 0 || index > 15)
						continue;

					color = ColorUtils.hexColorsl[index];
					break;
				}

				return new Color(color);
			}
		}

		switch (colorModeValue.get()) {
			case "Custom":
				return new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
			default:
				return ColorUtils.INSTANCE.fade(new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100);
		}
	}

   public static boolean shouldCancelNameTag(EntityLivingBase entity) {
      return LiquidBounce.moduleManager.getModule(ESP2D.class) != null && LiquidBounce.moduleManager.getModule(ESP2D.class).getState() && ((ESP2D)LiquidBounce.moduleManager.getModule(ESP2D.class)).tagsValue.get() && collectedEntities.contains(entity);
   }

   @Override
   public void onDisable() {
      collectedEntities.clear();
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      GL11.glPushMatrix();
      this.collectEntities();
      float partialTicks = event.getPartialTicks();
      ScaledResolution scaledResolution = new ScaledResolution(mc);
      int scaleFactor = scaledResolution.getScaleFactor();
      double scaling = (double)scaleFactor / Math.pow((double)scaleFactor, 2.0D);
      GL11.glScaled(scaling, scaling, scaling);
      int black = this.black;
      int background = this.backgroundColor;
      float scale = 0.65F;
      float upscale = 1.0F / scale;
      FontRenderer fr = mc.fontRendererObj;
      RenderManager renderMng = mc.getRenderManager();
      EntityRenderer entityRenderer = mc.entityRenderer;
      boolean outline = this.outline.get();
      boolean health = this.healthBar.get();
      boolean armor = this.armorBar.get();
      int i = 0;

      for(int collectedEntitiesSize = collectedEntities.size(); i < collectedEntitiesSize; ++i) {
         Entity entity = (Entity)collectedEntities.get(i);
         int color = getColor(entity).getRGB();
         if (RenderUtils.isInViewFrustrum(entity)) {
            double x = RenderUtils.interpolate(entity.posX, entity.lastTickPosX, (double)partialTicks);
            double y = RenderUtils.interpolate(entity.posY, entity.lastTickPosY, (double)partialTicks);
            double z = RenderUtils.interpolate(entity.posZ, entity.lastTickPosZ, (double)partialTicks);
            double width = (double)entity.width / 1.5D;
            double height = (double)entity.height + (entity.isSneaking() ? -0.3D : 0.2D);
            AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
            List vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));
            entityRenderer.setupCameraTransform(partialTicks, 0);
            Vector4d position = null;
            Iterator var38 = vectors.iterator();

            while(var38.hasNext()) {
               Vector3d vector = (Vector3d)var38.next();
               vector = this.project2D(scaleFactor, vector.x - renderMng.viewerPosX, vector.y - renderMng.viewerPosY, vector.z - renderMng.viewerPosZ);
               if (vector != null && vector.z >= 0.0D && vector.z < 1.0D) {
                  if (position == null) {
                     position = new Vector4d(vector.x, vector.y, vector.z, 0.0D);
                  }

                  position.x = Math.min(vector.x, position.x);
                  position.y = Math.min(vector.y, position.y);
                  position.z = Math.max(vector.x, position.z);
                  position.w = Math.max(vector.y, position.w);
               }
            }

            if (position != null) {
               entityRenderer.setupOverlayRendering();
               double posX = position.x;
               double posY = position.y;
               double endPosX = position.z;
               double endPosY = position.w;
               if (outline) {
                  if (this.boxMode.get() == "Box") {
                     RenderUtils.drawRect(posX - 1.0D, posY, posX + 0.5D, endPosY + 0.5D, black);
                     RenderUtils.drawRect(posX - 1.0D, posY - 0.5D, endPosX + 0.5D, posY + 0.5D + 0.5D, black);
                     RenderUtils.drawRect(endPosX - 0.5D - 0.5D, posY, endPosX + 0.5D, endPosY + 0.5D, black);
                     RenderUtils.drawRect(posX - 1.0D, endPosY - 0.5D - 0.5D, endPosX + 0.5D, endPosY + 0.5D, black);
                     RenderUtils.drawRect(posX - 0.5D, posY, posX + 0.5D - 0.5D, endPosY, color);
                     RenderUtils.drawRect(posX, endPosY - 0.5D, endPosX, endPosY, color);
                     RenderUtils.drawRect(posX - 0.5D, posY, endPosX, posY + 0.5D, color);
                     RenderUtils.drawRect(endPosX - 0.5D, posY, endPosX, endPosY, color);
                  } else {
                     RenderUtils.drawRect(posX + 0.5D, posY, posX - 1.0D, posY + (endPosY - posY) / 4.0D + 0.5D, black);
                     RenderUtils.drawRect(posX - 1.0D, endPosY, posX + 0.5D, endPosY - (endPosY - posY) / 4.0D - 0.5D, black);
                     RenderUtils.drawRect(posX - 1.0D, posY - 0.5D, posX + (endPosX - posX) / 3.0D + 0.5D, posY + 1.0D, black);
                     RenderUtils.drawRect(endPosX - (endPosX - posX) / 3.0D - 0.5D, posY - 0.5D, endPosX, posY + 1.0D, black);
                     RenderUtils.drawRect(endPosX - 1.0D, posY, endPosX + 0.5D, posY + (endPosY - posY) / 4.0D + 0.5D, black);
                     RenderUtils.drawRect(endPosX - 1.0D, endPosY, endPosX + 0.5D, endPosY - (endPosY - posY) / 4.0D - 0.5D, black);
                     RenderUtils.drawRect(posX - 1.0D, endPosY - 1.0D, posX + (endPosX - posX) / 3.0D + 0.5D, endPosY + 0.5D, black);
                     RenderUtils.drawRect(endPosX - (endPosX - posX) / 3.0D - 0.5D, endPosY - 1.0D, endPosX + 0.5D, endPosY + 0.5D, black);
                     RenderUtils.drawRect(posX, posY, posX - 0.5D, posY + (endPosY - posY) / 4.0D, color);
                     RenderUtils.drawRect(posX, endPosY, posX - 0.5D, endPosY - (endPosY - posY) / 4.0D, color);
                     RenderUtils.drawRect(posX - 0.5D, posY, posX + (endPosX - posX) / 3.0D, posY + 0.5D, color);
                     RenderUtils.drawRect(endPosX - (endPosX - posX) / 3.0D, posY, endPosX, posY + 0.5D, color);
                     RenderUtils.drawRect(endPosX - 0.5D, posY, endPosX, posY + (endPosY - posY) / 4.0D, color);
                     RenderUtils.drawRect(endPosX - 0.5D, endPosY, endPosX, endPosY - (endPosY - posY) / 4.0D, color);
                     RenderUtils.drawRect(posX, endPosY - 0.5D, posX + (endPosX - posX) / 3.0D, endPosY, color);
                     RenderUtils.drawRect(endPosX - (endPosX - posX) / 3.0D, endPosY - 0.5D, endPosX - 0.5D, endPosY, color);
                  }
               }

               boolean living = entity instanceof EntityLivingBase;
               boolean isPlayer = entity instanceof EntityPlayer;
               EntityLivingBase entityLivingBase;
               float armorValue;
               float itemDurability;
               double durabilityWidth;
               double textWidth;
               float tagY;
               if (living) {
                  entityLivingBase = (EntityLivingBase)entity;
                  if (health) {
                     armorValue = entityLivingBase.getHealth();
                     itemDurability = entityLivingBase.getMaxHealth();
                     if (armorValue > itemDurability) {
                        armorValue = itemDurability;
                     }

                     durabilityWidth = (double)(armorValue / itemDurability);
                     textWidth = (endPosY - posY) * durabilityWidth; 
                     String healthDisplay = dFormat.format(armorValue) + "§c❤";
                     //if (details.get()) Fonts.fontSmall.drawStringWithShadow(healthDisplay, (float)posX - 4F - Fonts.fontSmall.getStringWidth(healthDisplay), (float)(endPosY - textWidth) - Fonts.fontSmall.FONT_HEIGHT / 2F, -1);
                     if (details.get())
                     RenderUtils.drawRect(posX - 3.5D, posY - 0.5D, posX - 1.5D, endPosY + 0.5D, background);
                     if (armorValue > 0.0F) {
                        int healthColor = BlendUtils.getHealthColor(armorValue, itemDurability).getRGB();
                        RenderUtils.drawRect(posX - 3.0D, endPosY, posX - 2.0D, endPosY - textWidth, healthColor);
                        tagY = entityLivingBase.getAbsorptionAmount();
                        if (absorption.get() && tagY > 0.0F) {
                           RenderUtils.drawRect(posX - 3.0D, endPosY, posX - 2.0D, endPosY - (endPosY - posY) / 6.0D * (double)tagY / 2.0D, (new Color(Potion.absorption.getLiquidColor())).getRGB());
                        }
                     }
                  }
               }

               if (armor) {
                  if (living) {
                     entityLivingBase = (EntityLivingBase)entity;
                     armorValue = (float)entityLivingBase.getTotalArmorValue();
                     double armorWidth = (endPosY - posY) * (double)armorValue / 20.0D;
                     //if (details.get()) Fonts.font35.drawStringWithShadow(entityLivingBase.getTotalArmorValue() + "", (float)endPosX + 4F, (float)(endPosY - armorWidth) - 4F, -1);
                     RenderUtils.drawRect(endPosX + 1.5D, posY - 0.5D, endPosX + 3.5D, endPosY + 0.5D, background);
                     if (armorValue > 0.0F) {
                        RenderUtils.drawRect(endPosX + 2.0D, endPosY, endPosX + 3.0D, endPosY - armorWidth, new Color(40, 40, 230).getRGB());
                     }
                  } else if (entity instanceof EntityItem) {
                     ItemStack itemStack = ((EntityItem)entity).getEntityItem();
                     if (itemStack.isItemStackDamageable()) {
                        int maxDamage = itemStack.getMaxDamage();
                        itemDurability = (float)(maxDamage - itemStack.getItemDamage());
                        durabilityWidth = (endPosY - posY) * (double)itemDurability / (double)maxDamage;
                        //if (details.get()) Fonts.fontSmall.drawStringWithShadow(((int)itemDurability) + "", (float)endPosX + 4F, (float)(endPosY - durabilityWidth) - Fonts.fontSmall.FONT_HEIGHT / 2F, -1);
                        RenderUtils.drawRect(endPosX + 1.5D, posY - 0.5D, endPosX + 3.5D, endPosY + 0.5D, background);
                        RenderUtils.drawRect(endPosX + 2.0D, endPosY, endPosX + 3.0D, endPosY - durabilityWidth, new Color(40, 40, 230).getRGB());
                     }
                  }
               }

               
               if (isPlayer && details.get()) {
                  entityLivingBase = (EntityLivingBase) entity;
                  EntityPlayer player = (EntityPlayer) entityLivingBase;
                  double yDist = (double)(endPosY - posY) / 4.0D;
                  for (int j = 4; j > 0; j--) {
                     ItemStack armorStack = player.getEquipmentInSlot(j);
                     if (armorStack != null && armorStack.getItem() != null) {
                        renderItemStack(armorStack, endPosX + (armor ? 4.0D : 2.0D), posY + (yDist * (4 - j)) + (yDist / 2.0D) - 5.0D);
                     }
                  }
               }

               if (living && tagsValue.get()) {
                  entityLivingBase = (EntityLivingBase) entity;
               }

               if (itemTagsValue.get()) {
                  if (living) {
                     entityLivingBase = (EntityLivingBase) entity;
                  } else if (entity instanceof EntityItem) {
                  }
               }
            }
         }
      }

      GL11.glPopMatrix();
      GlStateManager.enableBlend();
      entityRenderer.setupOverlayRendering();
   }

   private void drawScaledString(String text, double x, double y, double scale, int color) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, x);
      GlStateManager.scale(scale, scale, scale);
      mc.fontRendererObj.drawStringWithShadow(text, 0, 0, color);
      GlStateManager.popMatrix();
   }

   private void drawScaledCenteredString(String text, double x, double y, double scale, int color) {
      drawScaledString(text, x - mc.fontRendererObj.getStringWidth(text) / 2F * scale, y, scale, color);
   }

   private void renderItemStack(ItemStack stack, double x, double y) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, x);
      GlStateManager.scale(0.5D, 0.5D, 0.5D);
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      RenderHelper.enableGUIStandardItemLighting();
      mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
      mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   private void collectEntities() {
      collectedEntities.clear();
      List playerEntities = mc.theWorld.loadedEntityList;
      int i = 0;

      for(int playerEntitiesSize = playerEntities.size(); i < playerEntitiesSize; ++i) {
         Entity entity = (Entity)playerEntities.get(i);
         if (EntityUtils.INSTANCE.isSelected(entity, false) || (localPlayer.get() && entity instanceof EntityPlayerSP && mc.gameSettings.thirdPersonView != 0) || (droppedItems.get() && entity instanceof EntityItem)) {
            collectedEntities.add(entity);
         }
      }

   }

   private Vector3d project2D(int scaleFactor, double x, double y, double z) {
      GL11.glGetFloat(2982, this.modelview);
      GL11.glGetFloat(2983, this.projection);
      GL11.glGetInteger(2978, this.viewport);
      return GLU.gluProject((float)x, (float)y, (float)z, this.modelview, this.projection, this.viewport, this.vector) ? new Vector3d((double)(this.vector.get(0) / (float)scaleFactor), (double)(((float)Display.getHeight() - this.vector.get(1)) / (float)scaleFactor), (double)this.vector.get(2)) : null;
   }
}
