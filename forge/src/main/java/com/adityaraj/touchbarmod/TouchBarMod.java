package com.adityaraj.touchbarmod;

import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

import javax.swing.*;

@Mod(modid = TouchBarMod.MODID, version = TouchBarMod.VERSION)
public class TouchBarMod {
    public static final String MODID = "touchbarmod";
    public static final String VERSION = "1.0";

    private PingUpdater pingUpdater;
    private FPSUpdater fpsUpdater;
    private TouchBarButton fpsButton;
    private TouchBarButton settingsButton;
    private JTouchBar touchBar;

    private boolean isFPSEnabled = true;
    private boolean initialized = false;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Touch Bar Mod initialized!");

        // Start persistent mcping.app
        pingUpdater = new PingUpdater();
        pingUpdater.launchMCPingApp();
        pingUpdater.startWritingPing();

        // Register tick event to delay Touch Bar setup
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);

        // Clean up on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Touch Bar Ping App...");
            pingUpdater.stopMCPingApp();
        }));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!initialized && mc.thePlayer != null && mc.currentScreen == null) {
            initialized = true;
            setupTouchBar();
        }
    }

    private void setupTouchBar() {
        SwingUtilities.invokeLater(() -> {
            try {
                touchBar = new JTouchBar();
                touchBar.setCustomizationIdentifier("com.adityaraj.touchbar.fps");

                // Settings button
                settingsButton = new TouchBarButton();
                settingsButton.setTitle("⚙");
                settingsButton.setAction((view) -> toggleFPSDisplay());

                touchBar.addItem(new TouchBarItem("settings", settingsButton, false));

                // FPS button
                fpsButton = new TouchBarButton();
                fpsButton.setTitle("FPS: --");
                fpsButton.setAction((view) -> {}); // keep interactive

                touchBar.addItem(new TouchBarItem("fps", fpsButton, true));

                long windowId = getNSWindowID();
                if (windowId != -1) {
                    touchBar.show(windowId);
                    System.out.println("Touch Bar FPS Display Shown");

                    fpsUpdater = new FPSUpdater(fpsButton);
                    fpsUpdater.start();
                } else {
                    System.out.println("Failed to get NSWindow for Minecraft.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void toggleFPSDisplay() {
        isFPSEnabled = !isFPSEnabled;
        System.out.println("FPS Display: " + (isFPSEnabled ? "ON" : "OFF"));

        if (fpsUpdater != null) {
            fpsUpdater.interrupt();
            fpsUpdater = null;
        }

        SwingUtilities.invokeLater(() -> {
            JTouchBar newTouchBar = new JTouchBar();
            newTouchBar.setCustomizationIdentifier("com.adityaraj.touchbar.fps");

            //RE-CREATE settingsButton
            TouchBarButton newSettingsButton = new TouchBarButton();
            newSettingsButton.setTitle("⚙");
            newSettingsButton.setAction((view) -> toggleFPSDisplay());
            newTouchBar.addItem(new TouchBarItem("settings", newSettingsButton, false));

            if (isFPSEnabled) {
                TouchBarButton newFPSButton = new TouchBarButton();
                newFPSButton.setTitle("FPS: --");
                newFPSButton.setAction((view) -> {});
                newTouchBar.addItem(new TouchBarItem("fps", newFPSButton, true));

                fpsUpdater = new FPSUpdater(newFPSButton);
                fpsUpdater.start();
            }

            long windowId = getNSWindowID();
            if (windowId != -1) {
                newTouchBar.show(windowId);
                touchBar = newTouchBar;
            }
        });
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
