package org.edx.mobile.comparator.TalkBackDetector;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import org.edx.mobile.interfaces.OnNavigateListener;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

public class CustomAccessibilityDelegate extends AccessibilityDelegateCompat {

    OnNavigateListener onNavigateListener;
    Object object;
    public CustomAccessibilityDelegate(Object object, OnNavigateListener onNavigateListener){
        this.onNavigateListener=onNavigateListener;
        this.object=object;
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
        switch (action) {
            case AccessibilityNodeInfoCompat.ACTION_CLICK:
                Log.d("AccessibilityDelegate", "Tap detected!");
                onNavigateListener.navigateToAnotherScreen(object);
                return true;
            default:
                return super.performAccessibilityAction(host, action, arguments);
        }
    }
    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        if(object!=null) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
            info.setClickable(true);
        }
        info.addAction(AccessibilityNodeInfoCompat.ACTION_FOCUS);
    }
}