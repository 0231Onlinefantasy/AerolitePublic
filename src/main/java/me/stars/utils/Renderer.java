package me.stars.utils;

import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import static net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public class Renderer extends MinecraftInstance {
    private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
    private static final Vec3 LIGHT0_POS = (new Vec3(0.20000000298023224, 1.0, -0.699999988079071)).normalize();
    private static final Vec3 LIGHT1_POS = (new Vec3(-0.20000000298023224, 1.0, 0.699999988079071)).normalize();
    public static void disableStandardItemLighting() {
        GlStateManager.disableLighting();
        GlStateManager.disableLight(0);
        GlStateManager.disableLight(1);
        GlStateManager.disableColorMaterial();
    }
    private static FloatBuffer setColorBuffer(float p_setColorBuffer_0_, float p_setColorBuffer_1_, float p_setColorBuffer_2_, float p_setColorBuffer_3_) {
        colorBuffer.clear();
        colorBuffer.put(p_setColorBuffer_0_).put(p_setColorBuffer_1_).put(p_setColorBuffer_2_).put(p_setColorBuffer_3_);
        colorBuffer.flip();
        return colorBuffer;
    }
    public static void enableGUIStandardItemLighting() {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
        enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
    public static void enableStandardItemLighting() {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        float lvt_0_1_ = 0.4F;
        float lvt_1_1_ = 0.6F;
        float lvt_2_1_ = 0.0F;
        GL11.glLight(16384, 4611, setColorBuffer((float) LIGHT0_POS.xCoord, (float) LIGHT0_POS.yCoord, (float) LIGHT0_POS.zCoord, 0.0f));
        GL11.glLight(16384, 4609, setColorBuffer(lvt_1_1_, lvt_1_1_, lvt_1_1_, 1.0F));
        GL11.glLight(16384, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GL11.glLight(16384, 4610, setColorBuffer(lvt_2_1_, lvt_2_1_, lvt_2_1_, 1.0F));
        GL11.glLight(16385, 4611, setColorBuffer((float) LIGHT1_POS.xCoord, (float) LIGHT1_POS.yCoord, (float) LIGHT1_POS.zCoord, 0.0f));
        GL11.glLight(16385, 4609, setColorBuffer(lvt_1_1_, lvt_1_1_, lvt_1_1_, 1.0F));
        GL11.glLight(16385, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GL11.glLight(16385, 4610, setColorBuffer(lvt_2_1_, lvt_2_1_, lvt_2_1_, 1.0F));
        GlStateManager.shadeModel(7424);
        GL11.glLightModel(2899, setColorBuffer(lvt_0_1_, lvt_0_1_, lvt_0_1_, 1.0F));
    }
    public static void addSmoothLine(float width) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(width);
    }

    public static void removeSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawCircleBorder(float x, float y, float radius, int color, float strength) {
        glColor(color);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        glLineWidth(strength);
        glBegin(GL11.GL_LINE_STRIP);
        for(int i = 0; i <= 360; i++)
            glVertex2d(x + Math.sin(i * Math.PI / 180.0D) * radius, y + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glColor4f(1F, 1F, 1F, 1F);
    }
}
