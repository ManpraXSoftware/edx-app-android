package org.humana.mobile.tta.utils;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import org.humana.mobile.R;

public class BottomNavigationViewHelper {
    private static TextView notificationBadge;
    private static View badge;
    @SuppressLint("RestrictedApi")
    static BottomNavigationItemView item;
    @SuppressLint("RestrictedApi")
    static BottomNavigationMenuView menuView;

    public static void disableShiftMode(BottomNavigationView view) {
        view.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        view.setItemHorizontalTranslationEnabled(false);
    }

    @SuppressLint("RestrictedApi")
    public static void addBadgeToBottomNav(BottomNavigationView view, int position, long count) {
        if (count != 0) {
            menuView = (BottomNavigationMenuView) view.getChildAt(0);
            item = (BottomNavigationItemView) menuView.getChildAt(position);

            if (item == null)
                return;

            badge = LayoutInflater.from(view.getContext())
                    .inflate(R.layout.notification_badge, item, false);
            item.addView(badge);
            notificationBadge = item.findViewById(R.id.badge);
            if (count > 9) {
                notificationBadge.setText("9+");
            } else {
                notificationBadge.setText(String.valueOf(count));
            }
        }
    }

    public static void removeBadgeFromBottomNav() {
        if (item != null) {
            item.removeView(badge);
        }
    }
}
