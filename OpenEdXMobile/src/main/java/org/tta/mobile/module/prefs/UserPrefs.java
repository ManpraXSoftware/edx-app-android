package org.tta.mobile.module.prefs;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.util.AppConstants;
import org.tta.mobile.util.FileUtil;
import org.tta.mobile.util.Sha1Util;

import java.io.File;
import java.io.IOException;

@Singleton
public class UserPrefs {

    private Context context;
    private final Logger logger = new Logger(getClass().getName());

    @NonNull
    private final LoginPrefs loginPrefs;

    @Inject
    public UserPrefs(Context context, @NonNull LoginPrefs loginPrefs) {
        this.context = context;
        this.loginPrefs = loginPrefs;
    }

    /**
     * Returns true if the "download over wifi only" is turned ON, false otherwise.
     *
     * @return
     */
    public boolean isDownloadOverWifiOnly() {
        // check if download is only allowed over wifi
        final PrefManager wifiPrefManager = new PrefManager(context,
                PrefManager.Pref.WIFI);
        boolean onlyWifi = wifiPrefManager.getBoolean(
                PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
        return onlyWifi;
    }

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user.
     * This is the folder where all video downloads should be kept.
     *
     * @return
     */
    @Nullable
    public File getDownloadDirectory() {
        final File externalAppDir = FileUtil.getExternalAppDir(context);
        final ProfileModel profile = getProfile();
        if (externalAppDir != null && profile != null) {
            File videosDir = new File(externalAppDir, AppConstants.Directories.VIDEOS);
            File usersVidsDir = new File(videosDir, Sha1Util.SHA1(profile.username));
            usersVidsDir.mkdirs();
            try {
                File noMediaFile = new File(usersVidsDir, ".nomedia");
                noMediaFile.createNewFile();
            } catch (IOException ioException) {
                Bundle parameters = new Bundle();
                parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, UserPrefs.class.getName());
                parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getDownloadDirectory");
                Logger.logCrashlytics(ioException, parameters);
                logger.error(ioException);
            }

            return usersVidsDir;
        }
        return null;
    }

    @Nullable
    public ProfileModel getProfile() {
        return loginPrefs.getCurrentUserProfile();
    }
}
