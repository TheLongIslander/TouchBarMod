package com.adityaraj.touchbarmod;

import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

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
                Random random = new Random();
                while (!Thread.currentThread().isInterrupted()) {
                    int fakePing = 50 + random.nextInt(100); // Replace with actual ping if needed
                    Files.write(Paths.get(pingFilePath), String.valueOf(fakePing).getBytes(), StandardOpenOption.CREATE);
                    Thread.sleep(1000); // update every 1s
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        pingWriterThread.start();
    }

    public void stopMCPingApp() {
        // Optionally stop the ping thread first
        if (pingWriterThread != null) {
            pingWriterThread.interrupt();
        }
        try {
            // Kill the app by its process name
            Runtime.getRuntime().exec("killall mcping");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
