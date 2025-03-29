package com.adityaraj.touchbarmod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = TouchBarMod.MODID, version = TouchBarMod.VERSION)
public class TouchBarMod {
    public static final String MODID = "touchbarmod";
    public static final String VERSION = "1.0";

    private PingUpdater pingUpdater;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Touch Bar Mod initialized!");

        pingUpdater = new PingUpdater();
        pingUpdater.launchMCPingApp();
        pingUpdater.startWritingPing();

        // Shut down mcping app when Minecraft quits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Touch Bar Ping App...");
            pingUpdater.stopMCPingApp();
        }));
    }
}
