package net.ccbluex.liquidbounce.slib.Guis;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.launch.data.uichoser;
import net.ccbluex.liquidbounce.slib.RenderUtils;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GuiLogin extends GuiScreen {
    public static EmptyInputBox username;
    static EmptyInputBox password;
    private boolean loginsuccessfully = false;
    public boolean hwidchecker = false;
    int anim = 140;
    String hwid = getHWID();

    public static String Name = "";
    public static String version = (LiquidBounce.CLIENT_NAME + LiquidBounce.CLIENT_REAL_VERSION + " | " + LiquidBounce.MINECRAFT_VERSION);
    public GuiButton loginButton;
    public GuiButton freeButton;
    public String UserName = null;
    public String Password = null;
    public static boolean isload = false;
    public String HWID = null;
    public static int LOVEU = 1;
    public static boolean Passed = false;
    public static String process = "[Waiting For Login]";
    public static String Now = LiquidBounce.CLIENT_NAME + "-Login";
    public GuiScreen prevGui;
    public int verifytimes = 5;
    public static String Network = "https://gitee.com/starslight/al-hwid/blob/master/main.txt";


    public static boolean bypass = true;//


    @Override
    public void initGui() {
        if (bypass) {//
            if (LiquidBounce.IN_Dev_VERSION)//
                Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_REAL_VERSION + " | " + LiquidBounce.DEV_SAYING);//
            else
                Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_REAL_VERSION + " | Minecraft " + LiquidBounce.MINECRAFT_VERSION +//
                        " | " + LiquidBounce.REL_SAYING);
            Minecraft.getMinecraft().displayGuiScreen(new uichoser());//
            LiquidBounce.mainMenu = new uichoser();//
        } else {   //
            super.initGui();
            username = new EmptyInputBox(4, mc.fontRendererObj, 20, 150, 100, 20);
            password = new EmptyInputBox(4, mc.fontRendererObj, 20, 180, 100, 20);
      } //
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        net.ccbluex.liquidbounce.slib.net.DSTHelper.Liquid();

        mc.getTextureManager().bindTexture(new ResourceLocation("main/game.png"));
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width, height);

        username.yPosition = 100;
        password.yPosition = username.yPosition + 30;
        //RenderUtils.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(62, 66, 104).getRGB());
        RenderUtils.drawRect(0, 0, 140, sr.getScaledHeight(), new Color(68, 68, 68,150).getRGB());

        Fonts.font40.drawString("Welcome,User!", 31, 45, ColorUtils.INSTANCE.rainbow().getRGB());
        Fonts.font40.drawString("Login to " + LiquidBounce.CLIENT_NAME, 30, 65, ColorUtils.INSTANCE.rainbow().getRGB());

        RenderUtils.drawRoundRect(username.xPosition, username.yPosition, username.xPosition + username.getWidth(), username.yPosition + 20, username.isFocused() ? new Color(71, 71, 71).getRGB() : new Color(149, 149, 149).getRGB());
        RenderUtils.drawRoundRect(username.xPosition + 0.5f, username.yPosition + 0.5f, username.xPosition + username.getWidth() - 0.5f, username.yPosition + 20 - 0.5f, new Color(33, 33, 33,180).getRGB());

        if (!username.isFocused() && username.getText().isEmpty()) {
            FontLoaders.F16.drawString("USERNAME", username.xPosition + 4, username.yPosition + 6, new Color(180, 180, 180).getRGB());
        }

        RenderUtils.drawRoundRect(password.xPosition, password.yPosition, password.xPosition + password.getWidth(), password.yPosition + 20, password.isFocused() ? new Color(71, 71, 71).getRGB() : new Color(149, 149, 149).getRGB());
        RenderUtils.drawRoundRect(password.xPosition + 0.5f, password.yPosition + 0.5f, password.xPosition + password.getWidth() - 0.5f, password.yPosition + 20 - 0.5f, new Color(68, 68, 68,180).getRGB());
        if (!password.isFocused() && password.getText().isEmpty()) {
            FontLoaders.F16.drawString("PASSWORD", password.xPosition + 4, password.yPosition + 6, new Color(180, 180, 180).getRGB());
        } else {
            String xing = "";
            for (char c : password.getText().toCharArray()) {
                xing = xing + "*";

            }
            FontLoaders.F20.drawString(xing, password.xPosition + 4, password.yPosition + 6, new Color(180, 180, 180).getRGB());
        }

        username.drawTextBox();
        if (isHovered(password.xPosition, password.yPosition + 30, password.xPosition + password.getWidth(), password.yPosition + 50, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0)) {

                verify();
            }
            RenderUtils.drawRoundRect(password.xPosition, password.yPosition + 30, password.xPosition + password.getWidth(), password.yPosition + 50, new Color(107, 141, 205).getRGB());
            FontLoaders.F16.drawCenteredString("LOGIN", password.xPosition + password.getWidth() / 2, password.yPosition + 38, new Color(255, 255, 255).getRGB());
        } else {
            RenderUtils.drawRoundRect(password.xPosition, password.yPosition + 30, password.xPosition + password.getWidth(), password.yPosition + 50, new Color(77, 111, 175).getRGB());
            FontLoaders.F16.drawCenteredString("LOGIN", password.xPosition + password.getWidth() / 2, password.yPosition + 38, new Color(255, 255, 255).getRGB());
        }


        if (isHovered(password.xPosition + password.getWidth() - FontLoaders.F14.getStringWidth("Copy hwid"), password.yPosition + 60, password.xPosition + password.getWidth(), password.yPosition + 70, mouseX, mouseY)) {
            if (Mouse.isButtonDown(0)) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                setClipboardString(hwid);
            }
            FontLoaders.F14.drawString("Copy hwid", password.xPosition + password.getWidth() - FontLoaders.F14.getStringWidth("Copy hwid"), password.yPosition + 60, new Color(77, 111, 175).getRGB());//77,111,175
        } else {
            FontLoaders.F14.drawString("Copy hwid", password.xPosition + password.getWidth() - FontLoaders.F14.getStringWidth("Copy hwid"), password.yPosition + 60, new Color(150, 150, 150).getRGB());//77,111,175
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_TAB:
                if (username.isFocused()) {
                    // Tab键切换焦点
                    if (keyCode == Keyboard.KEY_TAB) {
                        password.setFocused(true);
                        username.setFocused(false);
                        return;
                    }
                }
                break;
            case Keyboard.KEY_RETURN:

                verify();
                break;
            default:
                if (username.isFocused()) {
                    username.textboxKeyTyped(typedChar, keyCode);
                }
                if (password.isFocused()) {
                    password.textboxKeyTyped(typedChar, keyCode);
                }
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public static boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    private void verify() {
        try {
            ScaledResolution sr = new ScaledResolution(mc);
            verifytimes = 5;
            LOVEU = LOVEU * 10;//10
            HWID = getHWID();
            if (username.getText().isEmpty() || password.getText().isEmpty() || HWID.isEmpty()) {
                JOptionPane.showMessageDialog(null,"ERROR:One or more input boxes are not filled!");
            }
            if (!username.getText().isEmpty() && !password.getText().isEmpty() && !HWID.isEmpty()) {
                LOVEU = LOVEU * 10;//1000
                UserName = username.getText();
                Password = password.getText();

                String Verify = "[" + UserName + "]" + HWID + ":" + Password;
                String Banned = "[" + UserName + "]" + HWID + ":" + Password + "[BANNED]";
                if (get(Network).contains(Banned)) {
                    JOptionPane.showMessageDialog(null,"ERROR:This account had been banned!");
                }
                if (get(Network).contains(Verify)) {
                    LOVEU = LOVEU * 10;//10000
                    isload = true;
                    ClientUtils.INSTANCE.finishTitle();
                    Minecraft.getMinecraft().displayGuiScreen(new uichoser());
                    LiquidBounce.mainMenu = new uichoser();
              //      il1ll1ililllil11ili.INSTANCE.ll1ili1lil1i1li1();
              //      li1li1il1li11illl.INSTANCE.il1lli1li1li11ll();
              //      LAST.INSTANCE.firstCheck();

                } else {
                    JOptionPane.showMessageDialog(null,"ERROR:Wrong username or password!");
                    isload = false;
                }


            }
        } catch (Throwable var4) {
            var4.printStackTrace();
            JOptionPane.showMessageDialog(null,"未知错误!");
        }

    }

    private String get(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }

        in.close();

        return response.toString();
    }

    private String getHWID() {
        try {
            StringBuilder s = new StringBuilder();
            String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
            byte[] bytes = main.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] md5 = messageDigest.digest(bytes);
            int i = 0;
            for (byte b : md5) {
                s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
                if (i != md5.length - 1) {
                    s.append("-");
                }
                i++;
            }
            LOVEU = LOVEU * 10;//100
            return s.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
