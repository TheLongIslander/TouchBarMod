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
        let bundlePath = Bundle.main.bundleURL.deletingLastPathComponent()
        let path = bundlePath.appendingPathComponent("mcping.txt").path

        do {
            let contents = try String(contentsOfFile: path, encoding: .utf8)
            let cleaned = contents.trimmingCharacters(in: .whitespacesAndNewlines)

            if let ping = Int(cleaned) {
                DispatchQueue.main.async {
                    if ping == -1 {
                        // Show Minecraft icon
                        let icon = NSImage(named: "favicon")
                        self.pingButton.image = icon
                        self.pingButton.title = ""
                        self.pingButton.imageScaling = .scaleProportionallyUpOrDown
                        self.pingButton.imagePosition = .imageOnly
                    } else {
                        // Show text ping with color
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
                throw NSError(domain: "Invalid ping", code: 0)
            }
        } catch {
            let fallbackAttr = NSAttributedString(string: "-- ms", attributes: [
                .foregroundColor: NSColor.white,
                .font: NSFont.systemFont(ofSize: 12)
            ])
            DispatchQueue.main.async {
                self.pingButton.image = nil
                self.pingButton.attributedTitle = fallbackAttr
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
