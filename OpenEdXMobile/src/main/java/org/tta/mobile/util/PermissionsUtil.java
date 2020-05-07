package org.tta.mobile.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionsUtil {
    public static final int WRITE_STORAGE_PERMISSION_REQUEST = 1;
    public static final int CAMERA_PERMISSION_REQUEST = 2;
    public static final int READ_STORAGE_PERMISSION_REQUEST = 3;
    //SMS
    public static final int READ_SMS_PERMISSION_REQUEST = 5;

    public static boolean checkPermissions(String permission, @NonNull Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(int requestCode, @NonNull String[] permissions, @NonNull Fragment fragment) {
        fragment.requestPermissions(permissions, requestCode);
    }
}
