package org.tta.mobile.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.tta.mobile.R;
import org.tta.mobile.event.NewRelicEvent;
import org.tta.mobile.util.PermissionsUtil;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

public class BaseFragment extends Fragment {
    public interface PermissionListener {
        void onPermissionGranted(String[] permissions, int requestCode);

        void onPermissionDenied(String[] permissions, int requestCode);
    }

    private boolean isFirstVisit = true;
    protected PermissionListener permissionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectViewMembers(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstVisit) {
            isFirstVisit = false;
        } else {
            onRevisit();
        }
    }

    /**
     * Called when a Fragment is re-displayed to the user (the user has navigated back to it).
     * Defined to mock the behavior of {@link Activity#onRestart() Activity.onRestart} function.
     */
    protected void onRevisit() {
    }

    /**
     * Checks the status of the provided permissions, if a permission has been given, a callback
     * function is called otherwise the said permission is requested.
     *
     * @param permissions The requested permissions.
     * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
     */
    protected void askForPermission(String[] permissions, int requestCode) {
        if (getActivity() != null) {
            if (permissionListener != null && getGrantedPermissionsCount(permissions) == permissions.length) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            } else {
                PermissionsUtil.requestPermissions(requestCode, permissions, BaseFragment.this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && getGrantedPermissionsCount(permissions) == permissions.length) {
            if (permissionListener != null) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            }
        } else {
            // android.R.id.content gives you the root element of a view, without having to know its actual name/type/ID
            // Ref: https://stackoverflow.com/questions/47666685/java-lang-illegalargumentexception-no-suitable-parent-found-from-the-given-view
            Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show();
            if (permissionListener != null) {
                permissionListener.onPermissionDenied(permissions, requestCode);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public int getGrantedPermissionsCount(String[] permissions) {
        int grantedPermissionsCount = 0;
        for (String permission : permissions) {
            if (PermissionsUtil.checkPermissions(permission, getActivity())) {
                grantedPermissionsCount++;
            }
        }

        return grantedPermissionsCount;
    }
}
