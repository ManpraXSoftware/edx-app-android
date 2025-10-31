package org.edx.mobile.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.util.IntentFactory;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class Welcome_screen extends BaseFragmentActivity {
    private Button next_button;
    private ImageView mBackArrow;
    @Inject
    protected IEdxEnvironment environment;
    public static Intent newIntent(@Nullable @ScreenDef String screenName, @Nullable String pathId) {
        // These flags will make it so we only have a single instance of this activity,
        // but that instance will not be restarted if it is already running
        return IntentFactory.newIntentForComponent(Welcome_screen.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_SCREEN_NAME, screenName)
                .putExtra(EXTRA_PATH_ID, pathId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcom_screen);
        
        // Setup status bar padding for welcome screen
        setupStatusBarPadding();
        next_button = findViewById(R.id.next_button);
        mBackArrow = findViewById(R.id.back_arrow);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                environment.getRouter().showMainDashboard(Welcome_screen.this);
                finish();
            }
        });
    }
    
    /**
     * Setup status bar padding specifically for the welcome screen
     */
    private void setupStatusBarPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                // Enable edge-to-edge for Android 35+
                WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
                
                // Handle window insets to position content below status bar
                getWindow().getDecorView().setOnApplyWindowInsetsListener((view, insets) -> {
                    int statusBarType = WindowInsets.Type.statusBars();
                    android.graphics.Insets statusBarInsets = insets.getInsets(statusBarType);
                    
                    // Apply status bar padding to the window decor view
                    // This ensures content appears below status bar
                    View decorView = getWindow().getDecorView();
                    decorView.setPadding(
                        decorView.getPaddingLeft(),
                        statusBarInsets.top,
                        decorView.getPaddingRight(),
                        decorView.getPaddingBottom()
                    );
                    
                    return insets;
                });
                
            } catch (Exception e) {
                Log.e("WelcomeScreen", "Error setting up status bar padding", e);
            }
        }
    }
}
