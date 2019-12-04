package org.humana.mobile.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.humana.mobile.R;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.HtmlBlockModel;
import org.humana.mobile.services.ViewPagerDownloadManager;
import org.humana.mobile.view.custom.AuthenticatedWebView;
import org.humana.mobile.view.custom.URLInterceptorWebViewClient;

import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {
    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeContainer.setEnabled(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        authWebView.initWebView(getActivity(), true, false);
        authWebView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
            }

            @Override
            public void onPageFinished() {
                ViewPagerDownloadManager.instance.done(CourseUnitWebViewFragment.this, true);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(view.getUrl())) {
                    ViewPagerDownloadManager.instance.done(CourseUnitWebViewFragment.this, false);
                }
            }

            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse,
                                        boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    ViewPagerDownloadManager.instance.done(CourseUnitWebViewFragment.this, false);
                }
            }

            @Override
            public void onPageLoadProgressChanged(WebView view, int progress) {
            }
        });

        if (ViewPagerDownloadManager.USING_UI_PRELOADING) {
            if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
                ViewPagerDownloadManager.instance.addTask(this);
            } else {
                authWebView.loadUrl(true, unit.getBlockUrl());
            }
        }
    }

    @Override
    public void run() {
        if (this.isRemoving() || this.isDetached()) {
            ViewPagerDownloadManager.instance.done(this, false);
        } else {
            authWebView.loadUrl(true, unit.getBlockUrl());
        }
    }

    //the problem with viewpager is that it loads this fragment
    //and calls onResume even it is not visible.
    //which breaks the normal behavior of activity/fragment
    //lifecycle.
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (ViewPagerDownloadManager.USING_UI_PRELOADING) {
            return;
        }
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
            return;
        }
        if (isVisibleToUser) {
            if (authWebView != null) {
                authWebView.loadUrl(false, unit.getBlockUrl());
            }
        } else if (authWebView != null) {
            authWebView.tryToClearWebView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        authWebView.onResume();
        if (hasComponentCallback != null) {
            final CourseComponent component = hasComponentCallback.getComponent();
            if (component != null && component.equals(unit)) {
//                authWebView.loadUrl(false, unit.getBlockUrl());
                // ToDo hardcoded here for testing
                String blockId = unit.getId();
                String urlToload = environment.getConfig().getApiHostURL()+"/mx_humana_lms/xblock/"+blockId;
                authWebView.loadUrl(false, urlToload);
            }
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        authWebView.onDestroyView();
    }
}
