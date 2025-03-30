package com.adityaraj.touchbarmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.gui.GuiPlayerInfo;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class PingUpdater {
    private Thread pingWriterThread;
    private final String pingFilePath = "/Users/adityaraj/Downloads/TouchBarMod/mcping.txt";

    public void launchMCPingApp() {
        try {
            ProcessBuilder builder = new ProcessBuilder("open", "/Users/adityaraj/Downloads/TouchBarMod/mcping.app");
            builder.start();
            System.out.println("mcping.app launched.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWritingPing() {
        pingWriterThread = new Thread(() -> {
            try {
                Minecraft mc = Minecraft.getMinecraft();
                while (!Thread.currentThread().isInterrupted()) {
                    int ping = -1;

                    if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
                        NetHandlerPlayClient handler = mc.thePlayer.sendQueue;
                        List<GuiPlayerInfo> playerList = handler.playerInfoList;

                        for (GuiPlayerInfo info : playerList) {
                            if (info.name.equals(mc.thePlayer.getCommandSenderName())) {
                                ping = info.responseTime;
                                break;
                            }
                        }
                    }

                    Files.write(
                            Paths.get(pingFilePath),
                            String.valueOf(ping).getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );

                    Thread.sleep(1000); // update every 1 second
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        pingWriterThread.start();
    }

    public void stopMCPingApp() {
        if (pingWriterThread != null) {
            pingWriterThread.interrupt();
        }
        try {
            Runtime.getRuntime().exec("killall mcping");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
