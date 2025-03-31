package com.adityaraj.touchbarmod;

import com.thizzer.jtouchbar.item.view.TouchBarButton;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;

import javax.swing.SwingUtilities;
import java.lang.reflect.Field;

public class FPSUpdater extends Thread {
    private final TouchBarButton fpsButton;

    public FPSUpdater(TouchBarButton fpsButton) {
        this.fpsButton = fpsButton;
        fpsButton.setAction((view) -> {
            // No-op: makes button appear interactive so it doesn't look dimmed
        });
    }

    @Override
    public void run() {
        Minecraft mc = Minecraft.getMinecraft();
        Field fpsField;

        try {
            // Try both possible field names
            try {
                fpsField = ReflectionHelper.findField(mc.getClass(), "debugFPS");
            } catch (Exception e) {
                fpsField = ReflectionHelper.findField(mc.getClass(), "field_71470_ab");
            }
            fpsField.setAccessible(true);

            while (!Thread.currentThread().isInterrupted()) {
                int fps = fpsField.getInt(mc);

                // Run UI update on main thread to avoid UI bugs
                SwingUtilities.invokeLater(() -> fpsButton.setTitle("FPS: " + fps));

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> fpsButton.setTitle("FPS: ?"));
        }
    }
}
