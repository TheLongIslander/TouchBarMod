import Cocoa

@main
class AppDelegate: NSObject, NSApplicationDelegate, NSTouchBarDelegate {
    var window: NSWindow!
    let pingLabel = NSTextField(labelWithString: "Ping: -- ms")

    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.accessory)

        // Create dummy window to attach Touch Bar
        window = NSWindow(contentRect: NSRect(x: 0, y: 0, width: 400, height: 200),
                          styleMask: [.titled, .closable],
                          backing: .buffered,
                          defer: false)
        window.title = "TouchBarPing"
        window.makeKeyAndOrderFront(nil)

        // Show custom Touch Bar on that window
        window.touchBar = makeTouchBar()

        // Start ping update loop
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            self.updatePing()
        }
    }
    
    func updatePing() {
        let bundlePath = Bundle.main.bundleURL.deletingLastPathComponent()
        let path = bundlePath.appendingPathComponent("mcping.txt").path

        print("📂 Trying to read: \(path)")

        do {
            let contents = try String(contentsOfFile: path, encoding: .utf8)
            let cleaned = contents.trimmingCharacters(in: .whitespacesAndNewlines)
            print("Read ping: '\(cleaned)'")
            DispatchQueue.main.async {
                self.pingLabel.stringValue = "Ping: \(cleaned) ms"
            }
        } catch {
            print("Failed to read ping file at: \(path)\nError: \(error)")
            DispatchQueue.main.async {
                self.pingLabel.stringValue = "Ping: -- ms"
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
