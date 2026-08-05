package org.robolectric.shadows.support.v4;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import org.robolectric.Robolectric;

public class SupportFragmentTestUtil {
    public static void startVisibleFragment(Fragment fragment) {
        FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, null)
                .commitNow();
    }

    public static void startVisibleFragment(Fragment fragment, Class<? extends FragmentActivity> activityClass, int containerViewId) {
        FragmentActivity activity = Robolectric.setupActivity(activityClass);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment)
                .commitNow();
    }
}
