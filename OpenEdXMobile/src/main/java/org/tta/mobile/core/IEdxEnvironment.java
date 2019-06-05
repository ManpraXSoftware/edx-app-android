package org.tta.mobile.core;


import org.tta.mobile.module.analytics.AnalyticsRegistry;
import org.tta.mobile.module.db.IDatabase;
import org.tta.mobile.module.download.IDownloadManager;
import org.tta.mobile.module.notification.NotificationDelegate;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.module.prefs.UserPrefs;
import org.tta.mobile.module.storage.IStorage;
import org.tta.mobile.util.Config;
import org.tta.mobile.view.Router;

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    UserPrefs getUserPrefs();

    LoginPrefs getLoginPrefs();

    AnalyticsRegistry getAnalyticsRegistry();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();
}
