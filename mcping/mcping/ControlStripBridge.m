#import <Cocoa/Cocoa.h>
#import <Foundation/Foundation.h>
#import <dlfcn.h>
#import "ControlStripBridge.h"

@interface NSTouchBarItem ()
+ (void)addSystemTrayItem:(NSTouchBarItem *)item;
@end

typedef void (*DFRElementSetControlStripPresenceForIdentifierFunc)(NSString *identifier, BOOL isVisible);

void AddToControlStrip(NSView *view, NSString *identifier) {
    if (@available(macOS 10.12.2, *)) {
        NSCustomTouchBarItem *item = [[NSCustomTouchBarItem alloc] initWithIdentifier:identifier];
        item.view = view;

        [NSTouchBarItem addSystemTrayItem:item];

        // Dynamically load the private function
        void *handle = dlopen("/System/Library/PrivateFrameworks/DFRFoundation.framework/DFRFoundation", RTLD_LAZY);
        if (handle) {
            DFRElementSetControlStripPresenceForIdentifierFunc setPresenceFunc =
                (DFRElementSetControlStripPresenceForIdentifierFunc)dlsym(handle, "DFRElementSetControlStripPresenceForIdentifier");

            if (setPresenceFunc != NULL) {
                setPresenceFunc(identifier, YES);
            } else {
                NSLog(@"Failed to resolve DFRElementSetControlStripPresenceForIdentifier");
            }

            dlclose(handle);
        } else {
            NSLog(@"Failed to load DFRFoundation");
        }
    }
}
