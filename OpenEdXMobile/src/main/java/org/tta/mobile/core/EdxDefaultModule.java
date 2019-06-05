package org.tta.mobile.core;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import org.tta.mobile.authentication.LoginService;
import org.tta.mobile.course.CourseService;
import org.tta.mobile.discussion.DiscussionService;
import org.tta.mobile.discussion.DiscussionTextUtils;
import org.tta.mobile.http.provider.RetrofitProvider;
import org.tta.mobile.http.util.CallUtil;
import org.tta.mobile.http.provider.OkHttpClientProvider;
import org.tta.mobile.http.serialization.ISO8601DateTypeAdapter;
import org.tta.mobile.http.serialization.JsonPageDeserializer;
import org.tta.mobile.model.Page;
import org.tta.mobile.model.course.BlockData;
import org.tta.mobile.model.course.BlockList;
import org.tta.mobile.model.course.BlockType;
import org.tta.mobile.module.db.IDatabase;
import org.tta.mobile.module.db.impl.IDatabaseImpl;
import org.tta.mobile.module.download.IDownloadManager;
import org.tta.mobile.module.download.IDownloadManagerImpl;
import org.tta.mobile.module.notification.DummyNotificationDelegate;
import org.tta.mobile.module.notification.NotificationDelegate;
import org.tta.mobile.module.storage.IStorage;
import org.tta.mobile.module.storage.Storage;
import org.tta.mobile.tta.analytics.AnalyticsRetrofitProvider;
import org.tta.mobile.tta.data.remote.service.TaService;
import org.tta.mobile.tta.scorm.ScormService;
import org.tta.mobile.user.UserService;
import org.tta.mobile.util.AppStoreUtils;
import org.tta.mobile.util.BrowserUtil;
import org.tta.mobile.util.Config;
import org.tta.mobile.util.MediaConsentUtils;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class EdxDefaultModule extends AbstractModule {
    //if your module requires a context, add a constructor that will be passed a context.
    private Context context;

    //with RoboGuice 3.0, the constructor for AbstractModule will use an `Application`, not a `Context`
    public EdxDefaultModule(Context context) {
        this.context = context;
    }

    @Override
    public void configure() {
        Config config = new Config(context);

        bind(IDatabase.class).to(IDatabaseImpl.class);
        bind(IDownloadManager.class).to(IDownloadManagerImpl.class);

        bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);

        bind(IEdxEnvironment.class).to(EdxEnvironment.class);

        bind(LinearLayoutManager.class).toProvider(LinearLayoutManagerProvider.class);

        bind(EventBus.class).toInstance(EventBus.getDefault());

        bind(Gson.class).toInstance(new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(ISO8601DateTypeAdapter.FACTORY)
                .registerTypeAdapter(Page.class, new JsonPageDeserializer())
                .registerTypeAdapter(BlockData.class, new BlockData.Deserializer())
                .registerTypeAdapter(BlockType.class, new BlockType.Deserializer())
                .registerTypeAdapter(BlockList.class, new BlockList.Deserializer())
                .serializeNulls()
                .create());

        bind(OkHttpClientProvider.class).to(OkHttpClientProvider.Impl.class);
        bind(RetrofitProvider.class).to(RetrofitProvider.Impl.class);
        bind(OkHttpClient.class).toProvider(OkHttpClientProvider.Impl.class).in(Singleton.class);
        bind(Retrofit.class).toProvider(RetrofitProvider.Impl.class).in(Singleton.class);

        bind(LoginService.class).toProvider(LoginService.Provider.class).in(Singleton.class);
        bind(CourseService.class).toProvider(CourseService.Provider.class).in(Singleton.class);
        bind(DiscussionService.class).toProvider(DiscussionService.Provider.class).in(Singleton.class);
        bind(UserService.class).toProvider(UserService.Provider.class).in(Singleton.class);
        bind(ScormService.class).toProvider(ScormService.Provider.class).in(Singleton.class);

        bind(IStorage.class).to(Storage.class);
        //Room.databaseBuilder(context, AppDatabase.class, dbName).fallbackToDestructiveMigration()
        //                .build()
       // bind(ILocalDataSource.class).to(LocalDataSource.class);

       // bind(IRemoteDataSource.class).toInstance(RetrofitServiceUtil.create());

        //bind(TADatabase.class).toInstance(Room.databaseBuilder(context, TADatabase.class, "dbasfsfs").fallbackToDestructiveMigration()
          //      .build());

        //bind(AppPref.class).toProvider(AppPref.Provider.class);
        //bind(DataManager.class).toProvider(DataManager.Provider.class);

        bind(TaService.class).toProvider(TaService.TaProvider.class);

        bind(AnalyticsRetrofitProvider.class).to(AnalyticsRetrofitProvider.Impl.class);

        bind(IEdxDataManager.class).to(EdxDataManager.class);
        requestStaticInjection(CallUtil.class, BrowserUtil.class, MediaConsentUtils.class,
                DiscussionTextUtils.class, AppStoreUtils.class);
    }
}
