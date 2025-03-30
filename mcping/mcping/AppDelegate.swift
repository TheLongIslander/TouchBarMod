import Cocoa

@main
class AppDelegate: NSObject, NSApplicationDelegate, NSTouchBarDelegate {
    var window: NSWindow!
    var pingLabel: NSTextField!
    var pingButton: NSButton!

    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.accessory)

        // Optional dummy window
        window = NSWindow(contentRect: NSRect(x: 0, y: 0, width: 400, height: 200),
                          styleMask: [.titled, .closable],
                          backing: .buffered,
                          defer: false)
        window.title = "TouchBarPing"
        window.makeKeyAndOrderFront(nil)

        // Setup ping views
        setupPingViews()

        // Attach to window-local Touch Bar
        window.touchBar = makeTouchBar()

        // Attach to Control Strip (this is the persistent one on the right)
        AddToControlStrip(pingButton, "com.adityaraj.touchbar.ping")

        // Start ping update loop
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            self.updatePing()
        }
    }

    func setupPingViews() {
        // For the local Touch Bar
        pingLabel = NSTextField(labelWithString: "Ping: -- ms")
        pingLabel.font = NSFont.systemFont(ofSize: 12)
        pingLabel.alignment = .center
        pingLabel.isBezeled = false
        pingLabel.drawsBackground = false
        pingLabel.isEditable = false
        pingLabel.sizeToFit()

        // For the Control Strip (must be a button to render properly)
        pingButton = NSButton(title: "Ping: -- ms", target: nil, action: nil)
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
            DispatchQueue.main.async {
                self.pingLabel.stringValue = "Ping: \(cleaned) ms"
                self.pingButton.title = "Ping: \(cleaned) ms"
            }
        } catch {
            DispatchQueue.main.async {
                self.pingLabel.stringValue = "Ping: -- ms"
                self.pingButton.title = "Ping: -- ms"
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
