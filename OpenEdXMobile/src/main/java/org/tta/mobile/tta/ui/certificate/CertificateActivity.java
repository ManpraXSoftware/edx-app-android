package org.tta.mobile.tta.ui.certificate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.inject.Inject;

import org.tta.mobile.R;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Page;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.certificate.view_model.CertificateViewModel;
import org.tta.mobile.util.BrowserUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.WebViewUtil;
import org.tta.mobile.view.custom.AuthenticatedWebView;
import org.tta.mobile.view.custom.URLInterceptorWebViewClient;

public class CertificateActivity extends BaseVMActivity implements View.OnClickListener {

    private static final int INITIAL_ZOOM_SCALE = 90;

    private Certificate certificate;

    private CertificateViewModel viewModel;

    private Toolbar toolbar;
    private AuthenticatedWebView authWebView;
    private FloatingActionButton printCertificateFab;
    private View loadingIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        viewModel = new CertificateViewModel(this, certificate);
        binding(R.layout.t_activity_certificate, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        authWebView = findViewById(R.id.auth_webview);
        printCertificateFab = findViewById(R.id.print_certificate_fab);
        loadingIndicator = findViewById(R.id.loading_indicator);

        analytic.addMxAnalytics_db(
                certificate.getCourse_name(), Action.ViewCert, certificate.getCourse_name(),
                Source.Mobile, certificate.getCourse_id(), certificate.getCourse_id(), 0);

        printCertificateFab.setOnClickListener(this);
        initializeWebview();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeWebview(){

        authWebView.initWebView(this, false, false, false);
        WebView webview = authWebView.getWebView();

        //Zoom settings
        webview.setInitialScale(INITIAL_ZOOM_SCALE);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);

        //Other settings
        webview.getSettings().setJavaScriptEnabled( true );
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        //cache settings for webview
        webview.getSettings().setAppCacheMaxSize( 8 * 1024 * 1024 ); // 8MB
        webview.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        webview.getSettings().setAllowFileAccess( true );
        webview.getSettings().setAppCacheEnabled( true );
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        /*webview.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default

        if (!NetworkUtil.isConnected(this)) {
            // loading offline
            webview.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        }*/

        authWebView.loadUrl(false,
                viewModel.getDataManager().getConfig().getApiHostURL() + certificate.getDownload_url());

    }

    @Override
    public void onResume() {
        super.onResume();
        authWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        authWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        authWebView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.print_certificate_fab:
                if (authWebView.isInitiated() && !authWebView.isShowingError()) {
                    analytic.addMxAnalytics_db(certificate.getCourse_id(), Action.CertificateDownload,
                            String.valueOf(Page.CertificatePage), Source.Mobile, certificate.getCourse_id(),
                            certificate.getCourse_id(), 0);

                    WebViewUtil.createWebPrintJob(baseActivityContext, authWebView.getWebView(),
                            certificate.getCourse_name());
                }
                break;
        }
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey(Constants.KEY_CERTIFICATE)){
                certificate = parameters.getParcelable(Constants.KEY_CERTIFICATE);
            }
        }
    }
}
