import Cocoa

@main
class AppDelegate: NSObject, NSApplicationDelegate, NSTouchBarDelegate {
    var window: NSWindow!
    var pingLabel: NSTextField!
    var pingButton: NSButton!

    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.accessory)

        window = NSWindow(contentRect: NSRect(x: 0, y: 0, width: 400, height: 200),
                          styleMask: [.titled, .closable],
                          backing: .buffered,
                          defer: false)
        window.title = "TouchBarPing"
        window.makeKeyAndOrderFront(nil)

        setupPingViews()
        window.touchBar = makeTouchBar()
        AddToControlStrip(pingButton, "com.adityaraj.touchbar.ping")

        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            self.updatePing()
        }
    }

    func setupPingViews() {
        pingLabel = NSTextField(labelWithString: "-- ms")
        pingLabel.font = NSFont.systemFont(ofSize: 12)
        pingLabel.alignment = .center
        pingLabel.isBezeled = false
        pingLabel.drawsBackground = false
        pingLabel.isEditable = false
        pingLabel.sizeToFit()

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

                DispatchQueue.main.async {
                    self.pingLabel.stringValue = "\(ping) ms"
                    self.pingLabel.textColor = color

                    self.pingButton.attributedTitle = attrTitle
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
                self.pingLabel.stringValue = "-- ms"
                self.pingLabel.textColor = .white
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
