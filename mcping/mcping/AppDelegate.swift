import Cocoa

@main
class AppDelegate: NSObject, NSApplicationDelegate, NSTouchBarDelegate {
    var pingLabel: NSTextField!
    var pingButton: NSButton!

    func applicationDidFinishLaunching(_ notification: Notification) {
        // Run app completely headless (no dock icon, no windows)
        NSApp.setActivationPolicy(.prohibited)

        // Setup views for ping display
        setupPingViews()

        // Add custom item to Control Strip (persistent area)
        AddToControlStrip(pingButton, "com.adityaraj.touchbar.ping")

        // Start updating ping from file every second
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            self.updatePing()
        }
    }

    func setupPingViews() {
        // Local Touch Bar fallback (if you ever re-enable a window)
        pingLabel = NSTextField(labelWithString: "-- ms")
        pingLabel.font = NSFont.systemFont(ofSize: 12)
        pingLabel.alignment = .center
        pingLabel.isBezeled = false
        pingLabel.drawsBackground = false
        pingLabel.isEditable = false
        pingLabel.sizeToFit()

        // Control Strip item (must be a button for proper rendering)
        pingButton = NSButton(title: "-- ms", target: nil, action: nil)
        pingButton.isBordered = false
        pingButton.bezelStyle = .texturedRounded
        pingButton.font = NSFont.systemFont(ofSize: 12)
    }

    func updatePing() {
        let pipePath = "/tmp/mcping.pipe"

        DispatchQueue.global(qos: .background).async {
            while true {
                guard let fileHandle = FileHandle(forReadingAtPath: pipePath) else {
                    sleep(1)
                    continue
                }

                do {
                    let reader = try TextInputStream(fileHandle: fileHandle)

                    while let line = reader.readLine() {
                        if let ping = Int(line.trimmingCharacters(in: .whitespacesAndNewlines)) {
                            DispatchQueue.main.async {
                                if ping == -1 {
                                    let icon = NSImage(named: "favicon")
                                    self.pingButton.image = icon
                                    self.pingButton.title = ""
                                    self.pingButton.imageScaling = .scaleProportionallyUpOrDown
                                    self.pingButton.imagePosition = .imageOnly
                                } else {
                                    let color: NSColor
                                    switch ping {
                                        case ..<50: color = .systemGreen
                                        case 50..<150: color = .systemYellow
                                        default: color = .systemRed
                                    }

                                    let attrTitle = NSAttributedString(string: "\(ping) ms", attributes: [
                                        .foregroundColor: color,
                                        .font: NSFont.systemFont(ofSize: 12)
                                    ])

                                    self.pingButton.image = nil
                                    self.pingButton.attributedTitle = attrTitle
                                }
                            }
                        } else {
                            DispatchQueue.main.async {
                                let fallbackAttr = NSAttributedString(string: "-- ms", attributes: [
                                    .foregroundColor: NSColor.white,
                                    .font: NSFont.systemFont(ofSize: 12)
                                ])
                                self.pingButton.image = nil
                                self.pingButton.attributedTitle = fallbackAttr
                            }
                        }
                    }

                    // Reader finished (EOF) — reconnect on next loop
                    print("Pipe closed. Reopening...")

                } catch {
                    print("Failed to read from pipe: \(error)")
                    sleep(1)
                }
            }
        }
    }


    func makeTouchBar() -> NSTouchBar {
        let touchBar = NSTouchBar()
        touchBar.delegate = self
        touchBar.defaultItemIdentifiers = [.customPingItem]
        return touchBar
    }

    func touchBar(_ touchBar: NSTouchBar, makeItemForIdentifier identifier: NSTouchBarItem.Identifier) -> NSTouchBarItem? {
        if identifier == .customPingItem {
            let item = NSCustomTouchBarItem(identifier: identifier)
            item.view = pingLabel
            return item
        }
        return nil
    }
}

extension NSTouchBarItem.Identifier {
    static let customPingItem = NSTouchBarItem.Identifier("com.adityaraj.touchbar.ping")
}
