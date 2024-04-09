package org.edx.mobile.comparator.TalkBackDetector;


import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT) {

            // TalkBack is currently speaking
            // You can perform actions here when TalkBack starts speaking
            // For example, update UI to indicate that speech is in progress
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            // TalkBack finished speaking
            // You can perform actions here when TalkBack finishes speaking
            // For example, update UI to indicate that speech has ended
        }
    }

    @Override
    public void onInterrupt() {
        // Not implemented
    }
}