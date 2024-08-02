package org.edx.mobile.util;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;

public class AccessibleWebViewScrollView extends ScrollView {
    private AutoResizeWebView webView;

    public AccessibleWebViewScrollView(Context context) {
        super(context);
    }

    public AccessibleWebViewScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWebView(AutoResizeWebView webView) {
        this.webView = webView;
        addView(webView);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            int scrollY = getScrollY();
            int maxScrollY = webView.getContentHeight() - getHeight();
            if (scrollY < maxScrollY) {
                smoothScrollTo(0, scrollY + 100);
                return true;
            }
        }
        return super.performAccessibilityAction(action, arguments);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setScrollable(true);
        info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }
}