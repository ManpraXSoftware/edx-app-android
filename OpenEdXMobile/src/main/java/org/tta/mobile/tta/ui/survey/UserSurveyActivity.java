package org.tta.mobile.tta.ui.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.base.BaseFragmentActivity;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.util.IntentFactory;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.view.custom.IconProgressBar;

import static org.tta.mobile.util.BrowserUtil.config;
import static org.tta.mobile.util.BrowserUtil.loginAPI;

public class UserSurveyActivity extends BaseFragmentActivity {

    private WebView mxwebview;
    static String  webViewurl;
    private IconProgressBar progressWheel;
    private LinearLayout noInternetConnection_tv;


    public static Intent newIntent(String webViewUrlParam) {
        webViewurl=webViewUrlParam;
        return IntentFactory.newIntentForComponent(UserSurveyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.t_activity_user_survey);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle("Feedback");

        Intent myIntent = getIntent();

        mxwebview = findViewById(R.id.surveywebview);
        progressWheel= findViewById(R.id.loading_indicator);
        noInternetConnection_tv= findViewById(R.id.no_internet_layout);


        //check for internet ,otherwise connection message
        if (!NetworkUtil.isConnected(UserSurveyActivity.this))
        {
            noInternetConnection_tv.setVisibility(View.VISIBLE);
            mxwebview.setVisibility(View.GONE);
            return;
        }

        mxwebview.getSettings().setLoadsImagesAutomatically(true);
        mxwebview.getSettings().setJavaScriptEnabled(true);
        mxwebview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        WebSettings webSettings = mxwebview.getSettings();

        webSettings.setUserAgentString(webSettings.getUserAgentString() + "/" + "theteacherapp/1.0");
        webSettings.setJavaScriptEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        cookieManager.setCookie(config.getConnectUrl(),loginAPI.getConnectCookies());

        progressWheel.setVisibility(View.VISIBLE);

        mxwebview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && mxwebview.canGoBack()) {
                    mxwebview.goBack();
                    return true;
                }
                return false;
            }
        });

        mxwebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                progressWheel.setVisibility(View.VISIBLE);
                return true;
            }

            //Show loader on url load
            @Override
            public void onLoadResource(WebView view, String url) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    if (progressWheel.isShown()) {
                        progressWheel.setVisibility(View.GONE);

                    }
                } catch (Exception exception) {
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_CLASS_NAME, UserSurveyActivity.class.getName());
                    parameters.putString(Constants.KEY_FUNCTION_NAME, "onPageFinished");
                    parameters.putString(Constants.KEY_DATA, "Url = " + url);
                    Logger.logCrashlytics(exception, parameters);
                    exception.printStackTrace();
                }
                super.onPageFinished(view, url);
            }
        });
        //mxwebview.loadUrl(myIntent.getStringExtra("mxSurveyUrl"));

        mxwebview.loadUrl(webViewurl);
    }

}
