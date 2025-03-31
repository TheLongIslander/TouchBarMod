package com.adityaraj.touchbarmod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.Minecraft;
import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;


@Mod(modid = TouchBarMod.MODID, version = TouchBarMod.VERSION)
public class TouchBarMod {
    public static final String MODID = "touchbarmod";
    public static final String VERSION = "1.0";

    private PingUpdater pingUpdater;
    private JTouchBar touchBar;
    private TouchBarButton fpsButton;
    private FPSUpdater fpsUpdater;
    private TouchBarButton settingsButton;
    private boolean isFPSEnabled = true;
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Touch Bar Mod initialized!");

        // Start persistent mcping.app
        pingUpdater = new PingUpdater();
        pingUpdater.launchMCPingApp();
        pingUpdater.startWritingPing();

        // Set up floating Touch Bar for FPS
        setupTouchBar();

        // Shut down mcping app and cleanup on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Touch Bar Ping App...");
            pingUpdater.stopMCPingApp();
        }));
    }
    private void setupTouchBar() {
        try {
            touchBar = new JTouchBar();
            touchBar.setCustomizationIdentifier("com.adityaraj.touchbar.fps");

            // Settings Button (left-most)
            settingsButton = new TouchBarButton();
            settingsButton.setTitle("⚙");
            settingsButton.setAction((view) -> toggleFPSDisplay());

            touchBar.addItem(new TouchBarItem("settings", settingsButton, false)); // Not trailing — left-most

            // FPS Button (middle/right)
            fpsButton = new TouchBarButton();
            fpsButton.setTitle("FPS: --");
            fpsButton.setAction((view) -> {}); // prevent dimming

            touchBar.addItem(new TouchBarItem("fps", fpsButton, true)); // trailing: true

            long windowId = getNSWindowID();
            if (windowId != -1) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        touchBar.show(windowId);
                        System.out.println("Touch Bar FPS Display Shown");

                        fpsUpdater = new FPSUpdater(fpsButton);
                        fpsUpdater.start();
                    } catch (InterruptedException ignored) {}
                }).start();
            } else {
                System.out.println("Failed to find NSWindow for Minecraft");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void toggleFPSDisplay() {
        isFPSEnabled = !isFPSEnabled;

        System.out.println("FPS Display: " + (isFPSEnabled ? "ON" : "OFF"));

        if (fpsUpdater != null) {
            fpsUpdater.interrupt();
            fpsUpdater = null;
        }

        // Reuse the existing buttons (DON'T re-create them)
        JTouchBar newTouchBar = new JTouchBar();
        newTouchBar.setCustomizationIdentifier("com.adityaraj.touchbar.fps");

        // Re-add settings button
        newTouchBar.addItem(new TouchBarItem("settings", settingsButton, false));

        if (isFPSEnabled) {
            newTouchBar.addItem(new TouchBarItem("fps", fpsButton, true));
            fpsUpdater = new FPSUpdater(fpsButton);
            fpsUpdater.start();
        }

        // Show new TouchBar on UI thread
        long windowId = getNSWindowID();
        if (windowId != -1) {
            new Thread(() -> {
                try {
                    Thread.sleep(200); // small delay helps avoid UI race conditions
                    newTouchBar.show(windowId);
                    touchBar = newTouchBar; // replace reference *after* showing
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }
    private long getNSWindowID() {
        try {
            java.lang.reflect.Method implMethod = org.lwjgl.opengl.Display.class.getDeclaredMethod("getImplementation");
            implMethod.setAccessible(true);
            Object impl = implMethod.invoke(null);

            java.lang.reflect.Field windowField = impl.getClass().getDeclaredField("window");
            windowField.setAccessible(true);
            java.nio.ByteBuffer buffer = (java.nio.ByteBuffer) windowField.get(impl);
            return buffer.getLong(0);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
