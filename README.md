# TouchBarMod - Minecraft Touch Bar Ping Display

## Overview

**TouchBarMod** is a Forge 1.7.10 Minecraft mod that adds real-time server ping monitoring to the MacBook Touch Bar. It integrates a native macOS `.app` (called `mcping.app`) that displays your current Minecraft server latency directly on the Touch Bar's control strip.

## Features

- Automatically launches `mcping.app` when Minecraft starts
- Displays your ping on the Mac Touch Bar with color indicators:
  - Green: Good ping (< 50ms)
  - Yellow: Moderate ping (50–149ms)
  - Red: Poor ping (150ms+)
  - Minecraft icon if ping is unavailable
- Gracefully shuts down the app and cleans up on Minecraft exit
- Automatically downloads, unzips, and locally code-signs `mcping.app` if not found
- Streams ping updates every second via a real-time **named pipe** (`/tmp/mcping.pipe`)
- Fully auto-recovers if either Minecraft or the Touch Bar app is restarted

## Requirements

- macOS with Touch Bar support (macOS 10.12.2+)
- Minecraft Forge 1.7.10
- Java 8
- Internet connection (for first-time app download)

## Setup Instructions

1. **Install the Mod**

   Place the `TouchBarMod.jar` into your `mods/` directory with Forge 1.7.10 installed.

2. **Launch Minecraft**

   On first launch, the mod will:
   - Download `mcping.app.zip` from a hosted server
   - Extract and code-sign the app locally
   - Launch the app, which listens for ping updates over a named pipe

3. **Touch Bar Behavior**

   The Touch Bar will display:
   - Your ping in milliseconds
   - Minecraft icon if not connected
   - Color-coded ping levels

4. **Shutdown Behavior**

   On Minecraft exit:
   - `mcping.app` is closed automatically
   - The named pipe at `/tmp/mcping.pipe` is deleted

## File Structure

```
touchbarmod/
├── java/
│   └── com/adityaraj/touchbarmod/
│       ├── TouchBarMod.java        # Main mod class
│       └── PingUpdater.java        # Handles app launching and ping writing
└── native-app/
    ├── AppDelegate.swift           # Swift Touch Bar logic and pipe reader
    ├── TextInputStream.swift       # Line-by-line pipe reader helper
    ├── ControlStripBridge.m        # Adds button to system Touch Bar
    ├── ControlStripBridge.h        # Header for native bridge
    └── mcping-Bridging-Header.h    # Bridging header for Swift/Obj-C
```

## Security Note

This project uses macOS's `codesign` with a temporary local identity (`-`) for sandboxing purposes. This ensures the app is recognized by macOS as trusted even without a developer certificate.

## How the Pipe Works

- The mod creates a named pipe at `/tmp/mcping.pipe` using `mkfifo`
- It writes the latest ping every second to the pipe
- The Swift app reads lines from the pipe and updates the Touch Bar accordingly
- If either side restarts, the pipe auto-recovers — no manual reset needed

## Credits

- Developed by TheLongIslander
- Uses private macOS APIs via `DFRFoundation.framework` to modify the Touch Bar control strip
- Inspired by real-time status overlays for Minecraft

## Disclaimer

This project is for personal and educational use. Modifying macOS system features or using private APIs may result in unexpected behavior in future macOS updates.
