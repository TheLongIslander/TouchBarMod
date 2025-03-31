package com.adityaraj.touchbarmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.gui.GuiPlayerInfo;
import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.common.Color;
import com.thizzer.jtouchbar.common.Image;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class PingUpdater {
    private Thread pingWriterThread;

    private final Path mcDir = Minecraft.getMinecraft().mcDataDir.toPath();
    private final Path appPath = mcDir.resolve("mcping.app");
    private final Path zipPath = mcDir.resolve("mcping.zip");
    private final String curlUrl = "https://thelongislanderhome.asuscomm.com/tools/mcping.app.zip";

    public void launchMCPingApp() {
        try {
            // Step 1: Check if app exists
            if (Files.notExists(appPath)) {
                System.out.println("mcping.app not found. Downloading...");

                // Step 2: Download ZIP using curl
                new ProcessBuilder("curl", "-L", "-o", zipPath.toString(), curlUrl)
                        .inheritIO().start().waitFor();

                System.out.println("Downloaded ZIP to: " + zipPath);

                // Step 3: Unzip
                new ProcessBuilder("unzip", "-o", zipPath.toString(), "-d", mcDir.toString())
                        .inheritIO().start().waitFor();

                System.out.println("Unzipped to: " + appPath);

                // Step 4: Codesign
                new ProcessBuilder("codesign", "-s", "-", "--force", "--deep", appPath.toString())
                        .inheritIO().start().waitFor();

                System.out.println("Codesigned app.");
            }

            // Step 5: Launch app
            new ProcessBuilder("open", appPath.toString()).start();
            System.out.println("mcping.app launched.");

            // Step 6: Cleanup ZIP after successful launch
            if (Files.exists(zipPath)) {
                Files.delete(zipPath);
                System.out.println("Cleaned up ZIP.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startWritingPing() {
        pingWriterThread = new Thread(() -> {
            Path pipePath = Paths.get("/tmp/mcping.pipe");

            try {
                // Create the named pipe if it doesn't exist
                if (Files.notExists(pipePath)) {
                    new ProcessBuilder("mkfifo", pipePath.toString()).inheritIO().start().waitFor();
                    System.out.println("Created named pipe at: " + pipePath);
                }

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

                    // Try writing to the pipe (blocks until the reader connects)
                    try (BufferedWriter writer = Files.newBufferedWriter(pipePath, StandardOpenOption.WRITE)) {
                        writer.write(String.valueOf(ping));
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("No reader available yet, retrying...");
                        Thread.sleep(500);
                        continue;
                    }

                    Thread.sleep(1000); // wait 1 sec before next ping write
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
            System.out.println("mcping.app closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Path pipePath = Paths.get("/tmp/mcping.pipe");
            if (Files.exists(pipePath)) {
                Files.delete(pipePath);
                System.out.println("Deleted /tmp/mcping.pipe.");
            }
        } catch (IOException e) {
            System.err.println("Failed to delete /tmp/mcping.pipe:");
            e.printStackTrace();
        }
    }


}
