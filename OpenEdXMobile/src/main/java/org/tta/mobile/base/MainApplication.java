package org.tta.mobile.base;


import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.evernote.android.state.StateSaver;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;
import com.newrelic.agent.android.NewRelic;

import org.tta.mobile.BuildConfig;
import org.tta.mobile.R;
import org.tta.mobile.core.EdxDefaultModule;
import org.tta.mobile.core.IEdxEnvironment;
import org.tta.mobile.event.AppUpdatedEvent;
import org.tta.mobile.event.NewRelicEvent;
import org.tta.mobile.http.provider.OkHttpClientProvider;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.module.analytics.AnalyticsRegistry;
import org.tta.mobile.module.prefs.PrefManager;
import org.tta.mobile.module.storage.IStorage;
import org.tta.mobile.receivers.NetworkConnectivityReceiver;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.utils.LocaleHelper;
import org.tta.mobile.util.BrowserUtil;
import org.tta.mobile.util.Config;

import java.io.InputStream;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * This class initializes the modules of the app based on the configuration.
 */
public abstract class MainApplication extends MultiDexApplication {

    protected final Logger logger = new Logger(getClass().getName());

    public static MainApplication application;

    public static final MainApplication instance() {
        return application;
    }

    private Injector injector;

    @Inject
    protected Config config;

    @Inject
    protected AnalyticsRegistry analyticsRegistry;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Initializes the request manager, image cache,
     * all third party integrations and shared components.
     */
    private void init() {
        application = this;
        // FIXME: Disable RoboBlender to avoid annotation processor issues for now, as we already have plans to move to some other DI framework. See LEARNER-1687.
        // ref: https://github.com/roboguice/roboguice/wiki/RoboBlender-wiki#disabling-roboblender
        // ref: https://developer.android.com/studio/build/gradle-plugin-3-0-0-migration
        RoboGuice.setUseAnnotationDatabases(false);
        injector = RoboGuice.getOrCreateBaseApplicationInjector((Application) this, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(this), (Module) new EdxDefaultModule(this));

        injector.injectMembers(this);

        LocaleHelper.setLocale(getApplicationContext(), "hi");

        EventBus.getDefault().register(new CrashlyticsCrashReportObserver());

        if (config.getNewRelicConfig().isEnabled()) {
            EventBus.getDefault().register(new NewRelicObserver());
        }

        // initialize NewRelic with crash reporting disabled
        if (config.getNewRelicConfig().isEnabled()) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(config.getNewRelicConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        checkIfAppVersionUpgraded(this);

        // Register Font Awesome module in android-iconify library
        Iconify.with(new FontAwesomeModule());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Force Glide to use our version of OkHttp which now supports TLS 1.2 out-of-the-box for
        // Pre-Lollipop devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Glide.get(this).register(GlideUrl.class, InputStream.class,
                    new OkHttpUrlLoader.Factory(injector.getInstance(OkHttpClientProvider.class).get()));
        }

        Bridge.initialize(this, new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "hi"));
    }

    private void checkIfAppVersionUpgraded(Context context) {
        PrefManager.AppInfoPrefManager prefManager = new PrefManager.AppInfoPrefManager(context);
        long previousVersionCode = prefManager.getAppVersionCode();
        final long curVersionCode = BuildConfig.VERSION_CODE;
        if (previousVersionCode < 0) {
            // App opened first time after installation
            // Save version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App opened first time, VersionCode:" + curVersionCode);
        } else if (previousVersionCode < curVersionCode) {
            final String previousVersionName = prefManager.getAppVersionName();
            // Update version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App updated, VersionCode:" + previousVersionCode + "->" + curVersionCode);
            // App updated
            onAppUpdated(previousVersionCode, curVersionCode, previousVersionName, BuildConfig.VERSION_NAME);
        }
    }

    private void onAppUpdated(final long previousVersionCode, final long curVersionCode,
                              final String previousVersionName, final String curVersionName) {
        // Try repair of download data on updating of app version
        injector.getInstance(IStorage.class).repairDownloadCompletionData();
        // Fire app updated event
        EventBus.getDefault().postSticky(new AppUpdatedEvent(previousVersionCode, curVersionCode,
                previousVersionName, curVersionName));
    }

    public static class CrashlyticsCrashReportObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(Logger.CrashReportEvent e) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            if (BrowserUtil.loginPrefs.isLoggedIn()) {
                crashlytics.setUserId(BrowserUtil.loginPrefs.getUsername());
            } else {
                crashlytics.setUserId("Logged out state");
            }

            crashlytics.setCustomKey(Constants.KEY_CLASS_NAME, "");
            crashlytics.setCustomKey(Constants.KEY_FUNCTION_NAME, "");
            crashlytics.setCustomKey(Constants.KEY_DATA, "");

            Bundle parameters = e.getParameters();
            if (parameters != null){
                for (String key: parameters.keySet()){
                    String value = parameters.getString(key);
                    if (value != null) {
                        crashlytics.setCustomKey(key, value);
                    }
                }
            }
            crashlytics.recordException(e.getError());
        }
    }

    public static class NewRelicObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(NewRelicEvent e) {
            NewRelic.setInteractionName("Display " + e.getScreenName());
        }
    }

    public Injector getInjector() {
        return injector;
    }

    @NonNull
    public static IEdxEnvironment getEnvironment(@NonNull Context context) {
        return RoboGuice.getInjector(context.getApplicationContext()).getInstance(IEdxEnvironment.class);
    }
}
