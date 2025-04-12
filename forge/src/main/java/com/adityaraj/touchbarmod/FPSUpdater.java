package com.adityaraj.touchbarmod;

import com.thizzer.jtouchbar.item.view.TouchBarButton;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import java.lang.reflect.Field;

public class FPSUpdater extends Thread {
    private final TouchBarButton fpsButton;

    public FPSUpdater(TouchBarButton fpsButton) {
        this.fpsButton = fpsButton;
        fpsButton.setAction((view) -> {
            // No-op: keeps button from dimming
        });
        this.setName("TouchBar-FPSUpdater");
        this.setDaemon(true); // ensures JVM doesn't hang on exit
    }

    @Override
    public void run() {
        Minecraft mc = Minecraft.getMinecraft();
        Field fpsField;

        try {
            // Try both possible field names depending on obfuscation
            try {
                fpsField = ReflectionHelper.findField(mc.getClass(), "debugFPS");
            } catch (Exception e) {
                fpsField = ReflectionHelper.findField(mc.getClass(), "field_71470_ab");
            }
            fpsField.setAccessible(true);

            while (!Thread.currentThread().isInterrupted()) {
                int fps = fpsField.getInt(mc);

                // Ensure updates are on the UI thread
                SwingUtilities.invokeLater(() -> {
                    if (fpsButton != null) {
                        fpsButton.setTitle("FPS: " + fps);
                    }
                });

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                if (fpsButton != null) {
                    fpsButton.setTitle("FPS: ?");
                }
            });
        }
    }
}
