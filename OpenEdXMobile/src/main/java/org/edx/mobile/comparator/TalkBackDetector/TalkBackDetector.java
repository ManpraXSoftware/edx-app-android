package org.edx.mobile.comparator.TalkBackDetector;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class TalkBackDetector extends AccessibilityService {

    private final Context context;

    public TalkBackDetector(Context context) {
        this.context = context;
    }

    public boolean isTalkBackEnabled() {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {

            switch (serviceInfo.eventTypes) {
                case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                    // Start the handler to detect long press

                    break;
                case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                    // Stop the handler when touch exploration ends

                    break;
                case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                    break;
            }

        }

        return false;
    }

    public void addTalkBackStateChangeListener(AccessibilityManager.TouchExplorationStateChangeListener listener) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        am.addTouchExplorationStateChangeListener(listener);
    }

    public void removeTalkBackStateChangeListener(AccessibilityManager.TouchExplorationStateChangeListener listener) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        am.removeTouchExplorationStateChangeListener(listener);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
}
