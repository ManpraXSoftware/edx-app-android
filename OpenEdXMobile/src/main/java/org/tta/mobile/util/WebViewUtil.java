package org.tta.mobile.util;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.tta.mobile.http.HttpStatus;
import org.tta.mobile.http.HttpStatusException;
import org.tta.mobile.http.notifications.FullScreenErrorNotification;
import org.tta.mobile.http.provider.OkHttpClientProvider;
import org.tta.mobile.interfaces.WebViewStatusListener;
import org.tta.mobile.logger.Logger;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Common webview helper for any view that needs to use a webview.
 */
public class WebViewUtil {
    public final static String EMPTY_HTML = "<html><body></body></html>";

    /**
     * Creates the intial StringBuffer used when an view uses a webview. Uses a common
     * css file.
     */
    public static StringBuilder getIntialWebviewBuffer(Context context, Logger logger) {
        StringBuilder buff = new StringBuilder();
        buff.append("<head>");
        buff.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        try {
            String cssFileContent = FileUtil.loadTextFileFromAssets(context,
                    "css/render-html-in-webview.css");
            buff.append("<style>");
            buff.append(cssFileContent);
            buff.append("</style>");
        } catch (IOException e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, WebViewUtil.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getIntialWebviewBuffer");
            Logger.logCrashlytics(e, parameters);
            logger.error(e);
        }
        buff.append("</head>");
        return buff;
    }

    /**
     * Clears content of the WebView and loads {@link #EMPTY_HTML} in it.
     */
    public static void clearWebviewHtml(@Nullable WebView webView) {
        if (webView != null) {
            webView.loadData(EMPTY_HTML, "text/html", "UTF-8");
        }
    }

    /**
     * Simply loads a url within a WebView for Marshmallow & above and differently in case of
     * Lollipop & below.<br/>
     * WebViews prior to Marshmallow (API Level 23) don't provide a way to get the HTTP status
     * codes if the url being loaded fails.<br/>
     * This utility function solves this by making a server call using the url provided to query if
     * an error is being return and showLoading error or move on to load the url in WebView, if the server
     * is responding correctly.
     * <p>
     * Inspiration for this solution has been taken from this link:
     * https://stackoverflow.com/questions/11889020/get-http-status-code-in-android-webview/21609608#21609608
     *
     * @param context              Current context.
     * @param webView              The WebView to load the URL into.
     * @param url                  The URL to load.
     * @param viewInterface        WebView's callbacks interface.
     * @param errorNotification    The notification setup for showing/hiding errors.
     * @param okHttpClientProvider The utility to make server calls.
     */
    public static void loadUrlBasedOnOsVersion(@NonNull final Context context,
                                               @NonNull final WebView webView,
                                               @NonNull final String url,
                                               @NonNull final WebViewStatusListener viewInterface,
                                               @NonNull final FullScreenErrorNotification errorNotification,
                                               @NonNull OkHttpClientProvider okHttpClientProvider) {
        loadUrlBasedOnOsVersion(context, webView, url, viewInterface, errorNotification,
                okHttpClientProvider, 0, null);
    }

    /**
     * Simply loads a url within a WebView for Marshmallow & above and differently in case of
     * Lollipop & below.<br/>
     * WebViews prior to Marshmallow (API Level 23) don't provide a way to get the HTTP status
     * codes if the url being loaded fails.<br/>
     * This utility function solves this by making a server call using the url provided to query if
     * an error is being return and showLoading error or move on to load the url in WebView, if the server
     * is responding correctly.
     * <p>
     * Inspiration for this solution has been taken from this link:
     * https://stackoverflow.com/questions/11889020/get-http-status-code-in-android-webview/21609608#21609608
     *
     * @param context              Current context.
     * @param webView              The WebView to load the URL into.
     * @param url                  The URL to load.
     * @param viewInterface        WebView's callbacks interface.
     * @param errorNotification    The notification setup for showing/hiding errors.
     * @param okHttpClientProvider The utility to make server calls.
     * @param actionTextResId      The resource ID of the action button text.
     * @param actionListener       The callback to be invoked when the action button is clicked.
     */
    public static void loadUrlBasedOnOsVersion(@NonNull final Context context,
                                               @NonNull final WebView webView,
                                               @NonNull final String url,
                                               @NonNull final WebViewStatusListener viewInterface,
                                               @NonNull final FullScreenErrorNotification errorNotification,
                                               @NonNull OkHttpClientProvider okHttpClientProvider,
                                               @StringRes final int actionTextResId,
                                               @Nullable final View.OnClickListener actionListener) {
        if (!NetworkUtil.isConnected(context)) {
            errorNotification.showError(context, new IOException(), actionTextResId, actionListener);
        } else {
            errorNotification.hideError();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                viewInterface.showLoadingProgress();
                okHttpClientProvider.get().newCall(new Request.Builder()
                        .url(url)
                        .get()
                        .build())
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, final IOException e) {
                                webView.post((new Runnable() {
                                    @Override
                                    public void run() {
                                        errorNotification.showError(context, e, actionTextResId, actionListener);
                                        viewInterface.hideLoadingProgress();
                                        viewInterface.clearWebView();
                                    }
                                }));
                            }

                            @Override
                            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                                webView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        final int responseCode = response.code();
                                        if (responseCode >= HttpStatus.BAD_REQUEST) {
                                            errorNotification.showError(context,
                                                    new HttpStatusException(Response.error(responseCode,
                                                            ResponseBody.create(MediaType.parse("text/plain"),
                                                                    response.message()))),
                                                    actionTextResId, actionListener);
                                            viewInterface.hideLoadingProgress();
                                            viewInterface.clearWebView();
                                        } else {
                                            webView.loadUrl(url);
                                        }
                                    }
                                });
                            }
                        });
            } else {
                webView.loadUrl(url);
            }
        }
    }

    //Print content of webview
    public static void createWebPrintJob(Context context, WebView webView, String printJobName) {

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
        PrintJob printJob = printManager.print(printJobName, printAdapter, new PrintAttributes.Builder().build());

    }
}
