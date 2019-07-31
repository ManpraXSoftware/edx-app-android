package org.tta.mobile.tta.ui.deep_link;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import org.tta.mobile.R;
import org.tta.mobile.event.NewVersionAvailableEvent;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.deep_link.view_model.DeepLinkViewModel;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;

import de.greenrobot.event.EventBus;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class DeepLinkActivity extends BaseVMActivity {
    private static final int RANK = 0;

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_ISPUSH = "isPush";

    private  String path;
    private  String type;
    private boolean ispush=false;

    private DeepLinkViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DeepLinkViewModel(this);
        binding(R.layout.t_activity_deep_link, viewModel);

        if (requiresAuthentication()) {
            return;
        }

        //region get data form push notification if exist
        Intent intent = getIntent();
        handleIntent(intent);
        viewModel.getDataManager().getEdxEnvironment().getAnalyticsRegistry().trackScreenView(getString(R.string.label_my_courses));

        //TODO: Arjun need us this for deeplink data extractor
        // ATTENTION: This was auto-generated to handle app links.
/*        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();*/
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (requiresAuthentication()){
            return;
        }
        handleIntent(intent);
    }

    private boolean requiresAuthentication(){
        if (loginPrefs == null || !loginPrefs.isLoggedIn()) {
            if (!SigninRegisterActivity.isAlreadyOpened) {
                ActivityUtil.gotoPage(this, SigninRegisterActivity.class);
            }
            this.finish();
            return true;
        } else if (loginPrefs.getDisplayName() == null || loginPrefs.getDisplayName().equals(loginPrefs.getUsername())){
            if (!UserInfoActivity.isAlreadyOpened) {
                ActivityUtil.gotoPage(this, UserInfoActivity.class);
            }
            this.finish();
            return true;
        }
        return false;
    }

    private void handleIntent(Intent intent){
        Bundle push_notification_extras = intent.getExtras();
        if (push_notification_extras != null) {

            if (push_notification_extras.containsKey(EXTRA_ISPUSH)) {
                ispush = push_notification_extras.getBoolean(EXTRA_ISPUSH);
            }

            if (push_notification_extras.containsKey(Constants.EXTRA_NOTIFICATION)) {
                Notification notification = push_notification_extras.getParcelable(Constants.EXTRA_NOTIFICATION);
                if (notification != null) {
                    notification.setSeen(true);
                    viewModel.getDataManager().updateNotificationsInLocal(Collections.singletonList(notification));
                }
            }

            if (ispush) {
                if (push_notification_extras.containsKey(EXTRA_PATH)) {
                    path =push_notification_extras.getString(EXTRA_PATH);
                }

                if (push_notification_extras.containsKey(EXTRA_TYPE)) {
                    type = push_notification_extras.getString(EXTRA_TYPE);
                }

                if (type != null && path != null){
                    if (type.equalsIgnoreCase("course")){

                        if (!path.equals("")){
                            viewModel.fetchContent(path);
                        } else {
                            viewModel.gotoLandingPage();
                        }

                    } else if (type.equalsIgnoreCase("connect")){

                        String slug = getSlug(path);
                        if (slug != null && !slug.equals("")){
                            viewModel.fetchContent(slug);
                        } else {
                            viewModel.gotoLandingPage();
                        }

                    } else {
                        viewModel.gotoLandingPage();
                    }
                } else {
                    viewModel.gotoLandingPage();
                }
            } else {
                onClickLink();
            }
        } else {
            onClickLink();
        }
    }

    private void onClickLink(){
        Intent intent = getIntent();

        if (intent.getData() == null || intent.getData().getEncodedPath() == null) {
            viewModel.gotoLandingPage();
        } else {
            long contentId = extractContentId(intent.getData());
            if (contentId > 0) {
                viewModel.fetchContent(contentId);
            } else if (intent.getData().getEncodedPath().split("/").length > 1 &&
                    intent.getData().getEncodedPath().split("/")[2] != null &&
                    !intent.getData().getEncodedPath().split("/")[2].equals("")) {

                String host_url;
                String connect_url;
                String edx_url;

                connect_url = viewModel.getDataManager().getConfig().getConnectUrl();
                if (!connect_url.endsWith("/"))
                    connect_url = connect_url + "/";

                edx_url = viewModel.getDataManager().getConfig().getApiHostURL();
                if (!edx_url.endsWith("/"))
                    edx_url = edx_url + "/";

                host_url = intent.getData().getScheme() + "://" + intent.getData().getHost();
                if (!host_url.endsWith("/"))
                    host_url = host_url + "/";

                if (host_url.equals(connect_url) || host_url.equals("http://www.connect.theteacherapp.org/") ||
                        host_url.equals("http://connect.theteacherapp.org/"))
                    type = "connect";
                else if (host_url.equals(edx_url) || host_url.equals("http://www.theteacherapp.org/") ||
                        host_url.equals("http://theteacherapp.org/"))
                    type = "course";
                else {
                    viewModel.gotoLandingPage();
                    return;
                }

                if (type.equals("course"))
                    path = intent.getData().getEncodedPath().split("/")[2];
                else if(type.equals("connect"))
                    path = urldecode(intent.getData().getEncodedPath().split("/")[2]);

                if (path != null && !path.equals("")){
                    viewModel.fetchContent(path);
                } else {
                    viewModel.gotoLandingPage();
                }

            } else {
                viewModel.gotoLandingPage();
            }
        }
    }

    private long extractContentId(Uri uri) {
        try {
            return Long.parseLong(uri.getQueryParameter(Constants.KEY_CONTENT_ID));
        } catch (NumberFormatException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, DeepLinkActivity.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "extractContentId");
            parameters.putString(Constants.KEY_DATA, "Uri = " + uri);
            Logger.logCrashlytics(e, parameters);
            return 0;
        }
    }

    private String urldecode(String encoded)
    {
        try
        {
            return URLDecoder.decode(encoded, "utf-8");
        }
        catch(UnsupportedEncodingException e)
        {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, DeepLinkActivity.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "urldecode");
            parameters.putString(Constants.KEY_DATA, "Encoded = " + encoded);
            Logger.logCrashlytics(e, parameters);
            Log.d("deeplink ",e.toString());
        }
        return null;
    }

    private String getSlug(String path)
    {
        String mslug=new String();
        //http://connect.theteacherapp.org/contests/resourcecontest2/

        String[] slugArr=path.split("/");
        mslug=slugArr[slugArr.length-1];

        return mslug;
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            /* This is the main Activity, and is where the new version availability
             * notifications are being posted. These events are posted as sticky so
             * that they can be compared against new instances of them to be posted
             * in order to determine whether it has new information content. The
             * events have an intrinsic property to mark them as consumed, in order
             * to not have to remove the sticky events (and thus lose the last
             * posted event information). Finishing this Activity should be
             * considered as closing the current session, and the notifications
             * should be reposted on a new session. Therefore, we clear the session
             * information by removing the sticky new version availability events
             * from the event bus.
             */
            EventBus.getDefault().removeStickyEvent(NewVersionAvailableEvent.class);
        }
    }
}
