package org.edx.mobile.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoResizeWebView extends WebView {
    private OnSizeChangedListener mSizeChangedListener;

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height);
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mSizeChangedListener = listener;
    }

    public AutoResizeWebView(Context context) {
        super(context);
        init();
    }

    public AutoResizeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoResizeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getSettings().setJavaScriptEnabled(true);
        setBackgroundColor(Color.parseColor("#F2F2F2"));
        setContentDescription("Response WebView");
        setWebViewClient(new AccessibleWebViewClient());
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                resizeWebView();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(w, h);
        }
    }

    public void resizeWebView() {
        post(new Runnable() {
            @Override
            public void run() {
                evaluateJavascript(
                        "(function() {" +
                                "  var body = document.body;" +
                                "  var html = document.documentElement;" +
                                "  if (!body || !html) return null;" +
                                "  var width = Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth);" +
                                "  var height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);" +
                                "  return JSON.stringify({width: width, height: height});" +
                                "})();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (value == null || value.equals("null") || value.isEmpty()) {
                                    Log.e("AutoResizeWebView", "Received null or empty value from JavaScript");
                                    return;
                                }

                                try {
                                    // Remove surrounding quotes and unescape the string
                                    if (value.startsWith("\"") && value.endsWith("\"")) {
                                        value = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                                    }

                                    JSONObject dimensions = new JSONObject(value);
                                    int contentWidth = dimensions.getInt("width");
                                    int contentHeight = dimensions.getInt("height");

                                    // Convert to device pixels
                                    float density = getResources().getDisplayMetrics().density;
                                    int widthInPx = (int) (contentWidth * density);
                                    int heightInPx = (int) (contentHeight * density);

                                    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthInPx, MeasureSpec.EXACTLY);
                                    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightInPx, MeasureSpec.EXACTLY);
                                    measure(widthMeasureSpec, heightMeasureSpec);

                                    if (getLayoutParams().width != widthInPx || getLayoutParams().height != heightInPx) {
                                        getLayoutParams().width = widthInPx;
                                        getLayoutParams().height = heightInPx;
                                        requestLayout();
                                    }

                                    if (mSizeChangedListener != null) {
                                        mSizeChangedListener.onSizeChanged(widthInPx, heightInPx);
                                    }

                                    // Force the announcement of the content description
                                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                                } catch (JSONException e) {
                                    Log.e("AutoResizeWebView", "Error parsing dimensions: " + value, e);
                                }
                            }
                        }
                );
            }
        });
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        super.loadData(data, mimeType, encoding);
        post(new Runnable() {
            @Override
            public void run() {
                resizeWebView();
            }
        });
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
        post(new Runnable() {
            @Override
            public void run() {
                resizeWebView();
            }
        });
    }

    private class AccessibleWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.announceForAccessibility("Response WebView content loaded");

        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        boolean result = super.requestFocus(direction, previouslyFocusedRect);
        if (result) {
            clearFocus();
            setFocusable(true);
            setFocusableInTouchMode(true);
            result = super.requestFocus(direction, previouslyFocusedRect);
        }
        return result;
    }

}
