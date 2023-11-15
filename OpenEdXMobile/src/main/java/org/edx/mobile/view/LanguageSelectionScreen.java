package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.LocaleManager;

import java.util.HashMap;
import java.util.Map;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class LanguageSelectionScreen extends BaseFragmentActivity {
    private Button mNextButton;
    private TextView mBackArrow;
    private LinearLayout mEnglish;
    private LinearLayout mHindi;
    private LinearLayout mKannada;
    private LinearLayout mTamil;

    private LinearLayout mMalayalam;

    private LinearLayout mOdia;
    private LinearLayout mBengali;
    @Inject
    protected IEdxEnvironment environment;
    private String language = "";
    @Inject
    LoginPrefs loginPrefs;

    public static Intent newIntent(@Nullable @ScreenDef String screenName, @Nullable String pathId) {
        // These flags will make it so we only have a single instance of this activity,
        // but that instance will not be restarted if it is already running
        return IntentFactory.newIntentForComponent(LanguageSelectionScreen.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_SCREEN_NAME, screenName)
                .putExtra(EXTRA_PATH_ID, pathId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_selection_screen);
        mNextButton = findViewById(R.id.next_button);
        mBackArrow = findViewById(R.id.back_arrow);
        mEnglish = findViewById(R.id.english);
        mHindi = findViewById(R.id.hindi);
        mKannada = findViewById(R.id.kannada);
        mTamil = findViewById(R.id.tamil);
        mMalayalam=findViewById(R.id.malayalam);
        mOdia=findViewById(R.id.odia);
        mBengali = findViewById(R.id.bengali);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.english));
                onBackPressed();
            }
        });
        mEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEnglish.isSelected()) {
                    language = "en";
                    mEnglish.setSelected(true);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.english));
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
                 // mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                } else {
                    language = "";
                    mEnglish.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                  //mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });
        mHindi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHindi.isSelected()) {
                    language = "hi";
                    mHindi.setSelected(true);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
                 //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mEnglish.setSelected(false);
                    mKannada.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.hindi));
                } else {
                    language = "";
                    mHindi.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                 //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mEnglish.setSelected(false);
                    mKannada.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });
        mKannada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mKannada.isSelected()) {
                    language = "kn";
                    mKannada.setSelected(true);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
               //     mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mEnglish.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.kannada));
                } else {
                    language = "";
                    mKannada.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                  //  mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mEnglish.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });
        mTamil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTamil.isSelected()) {
                    language = "ta";
                    mTamil.setSelected(true);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
              //      mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.tamil));
                } else {
                    language = "";
                    mTamil.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                 //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });

        mMalayalam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mMalayalam.isSelected()) {
                    language = "ml";
                    mMalayalam.setSelected(true);
                    mOdia.setSelected(false);
                    mTamil.setSelected(false);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
                    //      mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mBengali.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.malayalam));
                } else {
                    language = "";
                    mTamil.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                    //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });

        mOdia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mOdia.isSelected()) {
                    language = "or";
                    mOdia.setSelected(true);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
                    //      mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mTamil.setSelected(false);
                    mMalayalam.setSelected(false);
                    mBengali.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.odia));
                } else {
                    language = "";
                    mTamil.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                    //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mBengali.setSelected(false);
                }
            }
        });
        mBengali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBengali.isSelected()) {
                    language = "bn";
                    mBengali.setSelected(true);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mTamil.setSelected(false);
                    mNextButton.setActivated(true);
                    mNextButton.setTextColor(Color.parseColor("#464A50"));
               //     mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mEnglish.setSelected(false);
                    sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.bengali));
                } else {
                    language = "";
                    mBengali.setSelected(false);
                    mTamil.setSelected(false);
                    mNextButton.setActivated(false);
                    mNextButton.setTextColor(Color.parseColor("#ffffffff"));
                    //   mNextButton.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_REMOVED);
                    mHindi.setSelected(false);
                    mKannada.setSelected(false);
                    mMalayalam.setSelected(false);
                    mOdia.setSelected(false);
                    mEnglish.setSelected(false);
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (mNextButton.isActivated()) {
                    if (language != null) {
                        LocaleManager.setNewLocale(LanguageSelectionScreen.this, language);
                    } else {
                        sendAnalyticsCourseDetail(getApplicationContext().getString(R.string.english));
                        LocaleManager.setNewLocale(LanguageSelectionScreen.this, "en");
                    }
                    loginPrefs.storeUserFirstTime("true");
                    environment.getRouter().showWelcome(LanguageSelectionScreen.this);
                    finish();
                }
            }
        });
    }
    void sendAnalyticsCourseDetail(String Language){
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.LANGAUGE_NAME,Language);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.SELECTED_LANGAUGE,null,"Language Change",values);
    }
}
