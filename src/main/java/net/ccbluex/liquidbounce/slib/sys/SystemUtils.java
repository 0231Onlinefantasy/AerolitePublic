package net.ccbluex.liquidbounce.slib.sys;


import java.awt.*;

public class SystemUtils {
    public static boolean main(String Title,String Text,TrayIcon.MessageType type) throws AWTException {
        if (SystemTray.isSupported()) {
            SystemUtils nd = new SystemUtils();
            displayTray(Title, Text, type);
        } else {
            System.err.println("Client Cracked");
            return false;
        }
        return false;
    }

    public static void displayTray(String Title,String Text,TrayIcon.MessageType type) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage(Title, Text, type);
    }
}