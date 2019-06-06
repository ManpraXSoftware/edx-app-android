package org.tta.mobile.module.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.tta.mobile.authentication.AuthResponse;
import org.tta.mobile.base.MainApplication;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.module.analytics.Analytics;
import org.tta.mobile.services.EdxCookieManager;
import org.tta.mobile.user.ProfileImage;
import org.tta.mobile.view.BulkDownloadFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VideoPrefs {
    @NonNull
    private final PrefManager pref;

    @Inject
    public VideoPrefs(@NonNull Context context) {
        pref = new PrefManager(context, PrefManager.Pref.VIDEOS);
    }


    public BulkDownloadFragment.SwitchState getBulkDownloadSwitchState(@NonNull String courseId) {
        final int ordinal = pref.getInt(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId));
        return (ordinal == -1 ?
                BulkDownloadFragment.SwitchState.DEFAULT :
                BulkDownloadFragment.SwitchState.values()[ordinal]);
    }

    public void setBulkDownloadSwitchState(@NonNull BulkDownloadFragment.SwitchState state,
                                           @NonNull String courseId) {
        pref.put(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId), state.ordinal());
    }
}
