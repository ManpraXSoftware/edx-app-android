package org.edx.mobile.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;


import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastStateListener;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;
import org.edx.mobile.googlecast.GoogleCastDelegate;
import org.edx.mobile.logger.Logger;

import de.greenrobot.event.EventBus;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity implements CastStateListener {

    private GoogleCastDelegate googleCastDelegate;
    private MenuItem mediaRouteMenuItem;
    private final Logger logger = new Logger(BaseAppActivity.class.getName());

    @Override
    protected void attachBaseContext(Context newBase) {
     //   super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        }
        //Or implement this for api 29 and above
        else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup status bar and content positioning
        setupStatusBarAndContent();
        
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
        googleCastDelegate = GoogleCastDelegate.getInstance(MainApplication.getEnvironment(this)
                .getAnalyticsRegistry());
        googleCastDelegate.addCastStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Replace the try-catch block with more appropriate logic so Travis-ci build get passed.
        // Can't access the CastButtonFactory while executing test cases of "CourseUnitNavigationActivityTest"
        // and throw exception.
        try {
            if (googleCastDelegate != null && (googleCastDelegate.isConnected() || showGoogleCastButton())) {
                getMenuInflater().inflate(R.menu.google_cast_menu_item, menu);
                mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                        menu, R.id.media_route_menu_item);
                // show the introduction overlay.
                if (isInForeground()) {
                    googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
                }
            }
        } catch (Exception e) {
            logger.error(e, true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // refresh the menu items to update the current state of google cast button
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        if (googleCastDelegate != null) {
            googleCastDelegate.removeCastStateListener(this);
        }
        super.onDestroy();
    }

    /**
     * @return True if screen needs to show the Google chrome un-casted button otherwise False.
     */
    public boolean showGoogleCastButton() {
        return false;
    }

    @Override
    public void onCastStateChanged(int newState) {
        /* App throws `IllegalArgumentException` when showing the Introductory Overlay in some cases.
         * Check the following issue for more details (still open).
         * Ref: https://issuetracker.google.com/issues/36191274
         * TODO: Replace the try-catch block with more appropriate logic / by updating the cast library
         * as part of the Jira story: https://openedx.atlassian.net/browse/LEARNER-7722
         */
        try {
            if (isInForeground()) {
                if (mediaRouteMenuItem != null) {
                    googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
                }
                invalidateOptionsMenu();
            }
        } catch (Exception e) {
            logger.error(e, true);
        }
    }
    
    /**
     * Setup status bar and content positioning for all Android versions
     */
    private void setupStatusBarAndContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Set status bar color
                int statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color);
                getWindow().setStatusBarColor(statusBarColor);
                
                // Configure status bar text color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    );
                }
                
                // Handle content below status bar for Android 35+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    setupEdgeToEdgeContent();
                }
                
            } catch (Exception e) {
                logger.error(e, true);
            }
        }
    }
    
    /**
     * Setup edge-to-edge content for Android 35+
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private void setupEdgeToEdgeContent() {
        try {
            // Enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            // Handle window insets to position content below status bar
            getWindow().getDecorView().setOnApplyWindowInsetsListener((view, insets) -> {
                int statusBarType = WindowInsets.Type.statusBars();
                android.graphics.Insets statusBarInsets = insets.getInsets(statusBarType);
                
                // Status bar padding will be handled by individual activities that need it
                // This prevents applying padding globally to all activities
                
                return insets;
            });
            
        } catch (Exception e) {
            logger.error(e, true);
        }
    }
}
