package org.tta.mobile.util.images;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.tta.mobile.R;
import org.tta.mobile.core.IEdxEnvironment;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.module.analytics.AnalyticsRegistry;
import org.tta.mobile.tta.analytics.Analytic;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.SourceName;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.util.ResourceUtil;

import java.util.List;

public enum ShareUtils {
    ;

    public static Intent newShareIntent(@NonNull String text) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain");
    }

    /**
     * Display a popup having a list of shareable apps through which a user can share course info.
     *  @param activity          Calling activity, pop up style will be based on this activity's style.
     * @param anchor            Anchor view around which popup will be shown.
     * @param courseData        Data of course which needs to be shared.
     * @param analyticsRegistry Analytics registry to fire required analytics events.
     * @param environment       Relevant configuration environment.
     * @param id
     */
    public static void showCourseShareMenu(@NonNull final Activity activity,
                                           @NonNull final View anchor,
                                           @NonNull final EnrolledCoursesResponse courseData,
                                           @NonNull final AnalyticsRegistry analyticsRegistry,
                                           @NonNull final IEdxEnvironment environment, long contentId) {
        final String COURSE_ABOUT_URL = courseData.getCourse().getCourse_about();
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                activity.getResources(),
                R.string.share_course_message,
                "platform_name",
                activity.getString(R.string.platform_name)).toString() + "\n" + COURSE_ABOUT_URL;
        ShareUtils.showShareMenu(
                activity, ShareUtils.newShareIntent(shareTextWithPlatformName), anchor,
                new ShareUtils.ShareMenuItemListener() {
                    @Override
                    public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                        final String shareText;
                        if (shareType == ShareUtils.ShareType.UNKNOWN) {
                            shareText = shareTextWithPlatformName;
                        } else {
                            shareText = getSharingText(shareType);
                        }
                        analyticsRegistry.courseDetailShared(courseData.getCourse().getId(), COURSE_ABOUT_URL, shareType);

                        Analytic analytic = new Analytic(activity);
                        analytic.addMxAnalytics_db(courseData.getCourse().getName(), Action.Share,
                                SourceName.course.name(), Source.Mobile, courseData.getCourse().getId(),
                                BreadcrumbUtil.getBreadcrumb() + "/" + shareType.name(),
                                courseData.getCourse().getId(), contentId);

                        if (!shareType.equals(ShareType.TTA)) {
                            final Intent intent = ShareUtils.newShareIntent(shareText);
                            intent.setComponent(componentName);
                            activity.startActivity(intent);
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.course_share_successful), Toast.LENGTH_LONG).show();
                        }
                    }

                    @NonNull
                    private String getSharingText(@NonNull ShareUtils.ShareType shareType) {
                        String courseUrl = COURSE_ABOUT_URL;
                        if (!TextUtils.isEmpty(shareType.getUtmParamKey())) {
                            final String utmParams = courseData.getCourse().getCourseSharingUtmParams(shareType.getUtmParamKey());
                            if (!TextUtils.isEmpty(utmParams)) {
                                courseUrl += "?" + utmParams;
                            }
                        }
                        final String platform;
                        final String twitterTag = environment.getConfig().getTwitterConfig().getHashTag();
                        if (shareType == ShareUtils.ShareType.TWITTER && !TextUtils.isEmpty(twitterTag)) {
                            platform = twitterTag;
                        } else {
                            platform = activity.getString(R.string.platform_name);
                        }
                        return ResourceUtil.getFormattedString(
                                activity.getResources(), R.string.share_course_message, "platform_name", platform).toString() +
                                "\n" + courseUrl;
                    }
                });
    }

    /**
     * Display a popup having a list of shareable apps that resolves the given sharing intent.
     *
     * @param activity    Calling activity, pop up style will be based on this activity's style.
     * @param shareIntent Sharing intent
     * @param anchor      Anchor view around which popup will be shown.
     * @param listener    Listener to take a callback when user select a particular app from
     *                    displayed popup.
     */
    @SuppressWarnings("RestrictedApi")
    public static void showShareMenu(@NonNull Activity activity, @NonNull Intent shareIntent,
                                     @NonNull View anchor, final @NonNull ShareMenuItemListener listener) {
        final PopupMenu popupMenu = new PopupMenu(activity, anchor);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
        for (final ResolveInfo resolveInfo : resolveInfoList) {
            final MenuItem shareItem = popupMenu.getMenu().add(resolveInfo.loadLabel(packageManager));
            shareItem.setIcon(resolveInfo.loadIcon(packageManager));
            shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    listener.onMenuItemClick(componentName, getShareTypeFromComponentName(componentName));
                    return false;
                }
            });
        }

        // TODO: Find an alternative to following usage of support MenuPopupHelper which is hidden in the support package
        // As PopupMenu doesn't support to showLoading icons in main menu, use MenuPopupHelper for it
        final MenuPopupHelper menuHelper = new MenuPopupHelper(activity, (MenuBuilder) popupMenu.getMenu(), anchor);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    public interface ShareMenuItemListener {
        void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareType shareType);
    }

    public enum ShareType {
        TWITTER("twitter"),
        FACEBOOK("facebook"),
        WHATSAPP("whatsapp"),
        TTA("tta"),
        UNKNOWN(null)
        ;

        private String utmParamKey;

        ShareType(@Nullable String key) {
            utmParamKey = key;
        }

        @Nullable
        public String getUtmParamKey() {
            return utmParamKey;
        }
    }

    @NonNull
    public static ShareType getShareTypeFromComponentName(@NonNull ComponentName componentName) {
        switch (componentName.getPackageName()) {
            case "com.facebook.katana":
            case "com.facebook.lite":
                return ShareType.FACEBOOK;
            case "com.twitter.android":
                return ShareType.TWITTER;
            case "com.whatsapp":
                return ShareType.WHATSAPP;
            case "org.tta.mobile":
                return ShareType.TTA;
            default:
                return ShareType.UNKNOWN;
        }
    }
}
