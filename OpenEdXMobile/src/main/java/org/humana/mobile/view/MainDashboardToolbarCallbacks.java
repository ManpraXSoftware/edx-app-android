package org.humana.mobile.view;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import android.widget.ImageView;
import android.widget.TextView;


public interface MainDashboardToolbarCallbacks {
    @Nullable
    SearchView getSearchView();

    @Nullable
    TextView getTitleView();

    @Nullable
    ImageView getProfileView();
}
