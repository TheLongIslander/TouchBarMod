# TouchBarMod - Minecraft Touch Bar Ping & FPS Display

## Overview

**TouchBarMod** is a Forge 1.7.10 Minecraft mod that adds real-time server ping and FPS monitoring to the MacBook Touch Bar. It integrates a native macOS `.app` (`mcping.app`) that displays your current Minecraft server latency on the system control strip, and also uses a floating Touch Bar overlay tied to the Minecraft window to show your current FPS.

## Features

- **Ping Display (Control Strip via `mcping.app`)**
  - Automatically launches `mcping.app` when Minecraft starts
  - Displays ping on the macOS Touch Bar control strip with color indicators:
    - Green: Good ping (< 50ms)
    - Yellow: Moderate ping (50–149ms)
    - Red: Poor ping (150ms+)
    - Minecraft icon if ping is unavailable
  - Streams ping updates every second via a real-time **named pipe** (`/tmp/mcping.pipe`)
  - Fully auto-recovers if either Minecraft or the Touch Bar app is restarted
  - Gracefully shuts down and cleans up on Minecraft exit

- **FPS Display (Floating Minecraft Touch Bar)**
  - Floating Touch Bar overlay (not the Control Strip)
  - Live FPS counter with `FPS: NNN` format
  - Gear (⚙) button toggles FPS display on/off
  - Display updates every second using Minecraft’s internal debug FPS counter
  - Resilient to focus loss or window changes

- **Automatic Setup**
  - Downloads, unzips, and locally code-signs `mcping.app` on first launch
  - Works out of the box on supported macOS systems

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

   - The **Control Strip** will display:
     - Your ping in milliseconds
     - A color-coded indicator for latency quality
     - A Minecraft icon if not connected
   - The **floating Minecraft Touch Bar** will display:
     - Live FPS in the format `FPS: NNN`
     - A gear button (⚙) that toggles FPS display on/off

4. **Shutdown Behavior**

   On Minecraft exit:
   - `mcping.app` is closed automatically
   - The named pipe at `/tmp/mcping.pipe` is deleted
   - The FPS thread is stopped and Touch Bar cleaned up

## File Structure

```
touchbarmod/
├── java/
│   └── com/adityaraj/touchbarmod/
│       ├── TouchBarMod.java        # Main mod class, sets up Touch Bar
│       ├── PingUpdater.java        # Launches mcping.app and writes ping to pipe
│       └── FPSUpdater.java         # Updates FPS in floating Touch Bar
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

## How FPS Display Works

- The mod uses [JTouchBar](https://github.com/Thizzer/JTouchBar) to create a floating Touch Bar attached to Minecraft’s window
- It uses reflection to extract Minecraft's internal `debugFPS` value
- The FPS is updated every second using a background thread
- You can toggle the FPS display on/off using the ⚙ button in the floating Touch Bar
- When off, only the ⚙ button is visible

## Credits

- Developed by TheLongIslander
- Uses private macOS APIs via `DFRFoundation.framework` to modify the Touch Bar control strip
- Inspired by real-time status overlays for Minecraft

## Disclaimer

This project is for personal and educational use. Modifying macOS system features or using private APIs may result in unexpected behavior in future macOS updates.
