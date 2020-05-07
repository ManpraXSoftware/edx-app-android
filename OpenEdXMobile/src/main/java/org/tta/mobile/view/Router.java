package org.tta.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.discussion.DiscussionTopic;
import org.tta.mobile.event.LogoutEvent;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.module.analytics.AnalyticsRegistry;
import org.tta.mobile.module.notification.NotificationDelegate;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.module.storage.IStorage;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.survey.UserSurveyActivity;
import org.tta.mobile.util.Config;
import org.tta.mobile.util.SecurityUtil;
import org.tta.mobile.view.dialog.AuthenticatedWebViewActivity;

import de.greenrobot.event.EventBus;

@Singleton
public class Router {
    public static final String EXTRA_BUNDLE = "bundle";
    public static final String EXTRA_COURSE_DATA = "course_data";
    public static final String EXTRA_COURSE_UNIT = "course_unit";
    public static final String EXTRA_COURSE_COMPONENT_ID = "course_component_id";
    public static final String EXTRA_DISCUSSION_TOPIC = "discussion_topic";

    @Inject
    Config config;

    @Inject
    private LoginAPI loginAPI;
    @Inject
    private LoginPrefs loginPrefs;
    @Inject
    private IStorage storage;

    public void showDownloads(Context context) {
        Intent downloadIntent = new Intent(context, DownloadListActivity.class);
        downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(downloadIntent);
    }

    public void showLoginScreen(Context context) {
        final Intent launchIntent = new Intent(context, SigninRegisterActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);
    }

    public void showCourseDiscussionAddPost(@NonNull Activity activity, @Nullable DiscussionTopic discussionTopic, @NonNull EnrolledCoursesResponse courseData) {
        Intent addPostIntent = new Intent(activity, DiscussionAddPostActivity.class);
        addPostIntent.putExtra(EXTRA_COURSE_DATA, courseData);
        addPostIntent.putExtra(EXTRA_DISCUSSION_TOPIC, discussionTopic);
        addPostIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(addPostIntent);
    }

    /**
     * Clear the login data and exit to the splash screen. This should only be called internally;
     * for handling manual logout,
     * {@link #performManualLogout(Context, AnalyticsRegistry, NotificationDelegate)} should be used instead.
     *
     * @param context  The context.
     * @param analyticsRegistry  The analytics provider object.
     * @param delegate The notification delegate.
     * @see #performManualLogout(Context, AnalyticsRegistry, NotificationDelegate)
     */
    public void forceLogout(Context context, AnalyticsRegistry analyticsRegistry, NotificationDelegate delegate) {
        loginPrefs.clear();

        EventBus.getDefault().post(new LogoutEvent());

        analyticsRegistry.trackUserLogout();
        analyticsRegistry.resetIdentifyUser();

        delegate.unsubscribeAll();

        showLoginScreen(context);
    }

    /**
     * Clears all the user data, revokes the refresh and access tokens, and exit to the splash
     * screen. This should only be called in response to manual logout by the user; for performing
     * logout internally (e.g. in response to refresh token expiration),
     * {@link #forceLogout(Context, AnalyticsRegistry, NotificationDelegate)} should be used instead.
     *
     * @param context  The context.
     * @param analyticsRegistry  The analytics provider object.
     * @param delegate The notification delegate.
     * @see #forceLogout(Context, AnalyticsRegistry, NotificationDelegate)
     */
    public void performManualLogout(Context context, AnalyticsRegistry analyticsRegistry, NotificationDelegate delegate) {
        // Remove all ongoing downloads first which requires username
        storage.removeAllDownloads();
        loginAPI.logOut();
        forceLogout(context, analyticsRegistry, delegate);
        SecurityUtil.clearUserData(context);
    }

    public void showAuthenticatedWebviewActivity(@NonNull Activity activity, @NonNull String url, @NonNull String title) {
        activity.startActivity(AuthenticatedWebViewActivity.newIntent(activity, url, title));
    }


    //TTA

    //Mx:Arjun change password
    public void resetAuthForChangePassword(Context context, AnalyticsRegistry analyticsRegistry, NotificationDelegate delegate) {
        loginAPI.logOut();
        loginPrefs.clear();

        EventBus.getDefault().post(new LogoutEvent());

        analyticsRegistry.trackUserLogout();
        analyticsRegistry.resetIdentifyUser();

        delegate.unsubscribeAll();
    }

    @NonNull
    public void getSurveyFeedbackActivity(Activity sourceActivity,String surveyurl) {
        sourceActivity.startActivity(UserSurveyActivity.newIntent(surveyurl));
    }
}
