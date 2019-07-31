package org.tta.mobile.tta.data;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import org.tta.mobile.R;
import org.tta.mobile.authentication.AuthResponse;
import org.tta.mobile.core.IEdxDataManager;
import org.tta.mobile.core.IEdxEnvironment;
import org.tta.mobile.course.CourseAPI;
import org.tta.mobile.course.CourseService;
import org.tta.mobile.discussion.CourseTopics;
import org.tta.mobile.discussion.DiscussionComment;
import org.tta.mobile.discussion.DiscussionThread;
import org.tta.mobile.discussion.DiscussionTopic;
import org.tta.mobile.discussion.DiscussionTopicDepth;
import org.tta.mobile.http.callback.Callback;
import org.tta.mobile.model.Page;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.course.CourseStructureV1Model;
import org.tta.mobile.model.course.HasDownloadEntry;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.module.db.DataCallback;
import org.tta.mobile.module.db.DbStructure;
import org.tta.mobile.module.db.impl.DbHelper;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.module.registration.model.RegistrationOption;
import org.tta.mobile.services.CourseManager;
import org.tta.mobile.services.VideoDownloadHelper;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.Analytic;
import org.tta.mobile.tta.analytics.SyncAnalyticsJob;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.data.enums.CertificateStatus;
import org.tta.mobile.tta.data.enums.ScormStatus;
import org.tta.mobile.tta.data.enums.SourceName;
import org.tta.mobile.tta.data.enums.SourceType;
import org.tta.mobile.tta.data.enums.SurveyType;
import org.tta.mobile.tta.data.local.db.ILocalDataSource;
import org.tta.mobile.tta.data.local.db.LocalDataSource;
import org.tta.mobile.tta.data.local.db.TADatabase;
import org.tta.mobile.tta.data.local.db.operation.GetCourseContentsOperation;
import org.tta.mobile.tta.data.local.db.operation.GetWPContentsOperation;
import org.tta.mobile.tta.data.local.db.table.Bookmark;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.local.db.table.StateContent;
import org.tta.mobile.tta.data.local.db.table.UnitStatus;
import org.tta.mobile.tta.data.model.BaseResponse;
import org.tta.mobile.tta.data.model.CountResponse;
import org.tta.mobile.tta.data.model.EmptyResponse;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.model.UpdateResponse;
import org.tta.mobile.tta.data.model.agenda.AgendaItem;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.data.model.authentication.FieldInfo;
import org.tta.mobile.tta.data.model.content.BookmarkResponse;
import org.tta.mobile.tta.data.model.content.CertificateStatusResponse;
import org.tta.mobile.tta.data.model.content.MyCertificatesResponse;
import org.tta.mobile.tta.data.model.content.TotalLikeResponse;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.data.model.library.CollectionItemsResponse;
import org.tta.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.tta.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.tta.mobile.tta.data.model.profile.FeedbackResponse;
import org.tta.mobile.tta.data.model.profile.FollowStatus;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.data.model.profile.UserAddressResponse;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.data.remote.IRemoteDataSource;
import org.tta.mobile.tta.data.remote.RetrofitServiceUtil;
import org.tta.mobile.tta.data.remote.api.MxCookiesAPI;
import org.tta.mobile.tta.data.remote.api.MxSurveyAPI;
import org.tta.mobile.tta.exception.TaException;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.receiver.DeleteFeedsReceiver;
import org.tta.mobile.tta.scorm.ContentType;
import org.tta.mobile.tta.scorm.ScormBlockModel;
import org.tta.mobile.tta.scorm.ScormStartResponse;
import org.tta.mobile.tta.task.GetVersionUpdatedTask;
import org.tta.mobile.tta.task.agenda.GetMyAgendaContentTask;
import org.tta.mobile.tta.task.agenda.GetMyAgendaCountTask;
import org.tta.mobile.tta.task.agenda.GetStateAgendaContentTask;
import org.tta.mobile.tta.task.agenda.GetStateAgendaCountTask;
import org.tta.mobile.tta.task.authentication.GetGenericUserFieldInfoTask;
import org.tta.mobile.tta.task.authentication.LoginTask;
import org.tta.mobile.tta.task.content.GetContentFromSourceIdentityTask;
import org.tta.mobile.tta.task.content.GetContentTask;
import org.tta.mobile.tta.task.content.GetMyContentStatusTask;
import org.tta.mobile.tta.task.content.GetUserContentStatusTask;
import org.tta.mobile.tta.task.content.IsContentMyAgendaTask;
import org.tta.mobile.tta.task.content.IsLikeTask;
import org.tta.mobile.tta.task.content.SetBookmarkTask;
import org.tta.mobile.tta.task.content.SetLikeTask;
import org.tta.mobile.tta.task.content.SetLikeUsingSourceIdentityTask;
import org.tta.mobile.tta.task.content.SetUserContentTask;
import org.tta.mobile.tta.task.content.TotalLikeTask;
import org.tta.mobile.tta.task.content.course.GetCourseDataFromPersistableCacheTask;
import org.tta.mobile.tta.task.content.course.UserEnrollmentCourseFromCacheTask;
import org.tta.mobile.tta.task.content.course.UserEnrollmentCourseTask;
import org.tta.mobile.tta.task.content.course.certificate.GenerateCertificateTask;
import org.tta.mobile.tta.task.content.course.certificate.GetCertificateStatusTask;
import org.tta.mobile.tta.task.content.course.certificate.GetCertificateTask;
import org.tta.mobile.tta.task.content.course.certificate.GetMyCertificatesTask;
import org.tta.mobile.tta.task.content.course.discussion.CreateDiscussionCommentTask;
import org.tta.mobile.tta.task.content.course.discussion.GetCommentRepliesTask;
import org.tta.mobile.tta.task.content.course.discussion.GetDiscussionThreadTask;
import org.tta.mobile.tta.task.content.course.discussion.GetDiscussionThreadsTask;
import org.tta.mobile.tta.task.content.course.discussion.GetDiscussionTopicsTask;
import org.tta.mobile.tta.task.content.course.discussion.GetThreadCommentsTask;
import org.tta.mobile.tta.task.content.course.discussion.LikeDiscussionCommentTask;
import org.tta.mobile.tta.task.content.course.discussion.LikeDiscussionThreadTask;
import org.tta.mobile.tta.task.content.course.scorm.GetUnitStatusTask;
import org.tta.mobile.tta.task.content.course.scorm.StartScormTask;
import org.tta.mobile.tta.task.feed.FollowUserTask;
import org.tta.mobile.tta.task.feed.GetFeedsTask;
import org.tta.mobile.tta.task.feed.GetSuggestedUsersTask;
import org.tta.mobile.tta.task.library.GetCollectionConfigTask;
import org.tta.mobile.tta.task.library.GetCollectionItemsTask;
import org.tta.mobile.tta.task.library.GetConfigModifiedDateTask;
import org.tta.mobile.tta.task.notification.CreateNotificationsTask;
import org.tta.mobile.tta.task.notification.GetNotificationsTask;
import org.tta.mobile.tta.task.notification.UpdateNotificationsTask;
import org.tta.mobile.tta.task.profile.ChangePasswordTask;
import org.tta.mobile.tta.task.profile.GetAccountTask;
import org.tta.mobile.tta.task.profile.GetFollowStatusTask;
import org.tta.mobile.tta.task.profile.GetFollowersOrFollowingTask;
import org.tta.mobile.tta.task.profile.GetProfileTask;
import org.tta.mobile.tta.task.profile.GetUserAddressTask;
import org.tta.mobile.tta.task.profile.SubmitFeedbackTask;
import org.tta.mobile.tta.task.profile.UpdateMyProfileTask;
import org.tta.mobile.tta.task.search.GetSearchFilterTask;
import org.tta.mobile.tta.task.search.SearchPeopleTask;
import org.tta.mobile.tta.task.search.SearchTask;
import org.tta.mobile.tta.ui.otp.AppSignatureHelper;
import org.tta.mobile.tta.utils.AlarmManagerUtil;
import org.tta.mobile.tta.utils.FirebaseUtil;
import org.tta.mobile.tta.utils.RxUtil;
import org.tta.mobile.tta.utils.UrlUtil;
import org.tta.mobile.tta.wordpress_client.data.db_command.DB_Commands;
import org.tta.mobile.tta.wordpress_client.model.Comment;
import org.tta.mobile.tta.wordpress_client.model.CustomComment;
import org.tta.mobile.tta.wordpress_client.model.Post;
import org.tta.mobile.tta.wordpress_client.model.User;
import org.tta.mobile.tta.wordpress_client.model.WPProfileModel;
import org.tta.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.tta.mobile.tta.wordpress_client.rest.HttpServerErrorResponse;
import org.tta.mobile.tta.wordpress_client.rest.WordPressRestResponse;
import org.tta.mobile.tta.wordpress_client.rest.WpClientRetrofit;
import org.tta.mobile.user.Account;
import org.tta.mobile.user.ProfileImage;
import org.tta.mobile.user.SetAccountImageTask;
import org.tta.mobile.util.Config;
import org.tta.mobile.util.DateUtil;
import org.tta.mobile.util.NetworkUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static org.tta.mobile.tta.Constants.TA_DATABASE;

/**
 * Created by Arjun on 2018/9/18.
 */

public class DataManager extends BaseRoboInjector {
    private static DataManager mDataManager;
    @Inject
    public IEdxDataManager edxDataManager;
    private Context context;
    private IRemoteDataSource mRemoteDataSource;
    private ILocalDataSource mLocalDataSource;
    @com.google.inject.Inject
    private IEdxEnvironment edxEnvironment;

    @com.google.inject.Inject
    private Config config;

    @com.google.inject.Inject
    private CourseManager courseManager;

    @com.google.inject.Inject
    private CourseAPI courseApi;

    @com.google.inject.Inject
    private VideoDownloadHelper downloadManager;

    private WpClientRetrofit wpClientRetrofit;
    private DB_Commands db_commands;

    private AppPref mAppPref;
    private LoginPrefs loginPrefs;

    private DbHelper dbHelper;

    private Analytic analytic;

    private Handler mHandler;

    private DataManager(Context context, IRemoteDataSource remoteDataSource, ILocalDataSource localDataSource) {
        super(context);
        this.context = context;
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;

        mAppPref = new AppPref(context);
        loginPrefs = new LoginPrefs(context);

        dbHelper = new DbHelper(context);

        db_commands = new DB_Commands(context);

        analytic = new Analytic(context);

        mHandler = new Handler();
    }

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager(context, RetrofitServiceUtil.create(context, true),
                            new LocalDataSource(Room.databaseBuilder(context, TADatabase.class, TA_DATABASE).fallbackToDestructiveMigration()
                                    .build()));
                }
            }
        }
        mDataManager.wpClientRetrofit = new WpClientRetrofit(true, false, context);
        return mDataManager;
    }

    public void refreshLocalDatabase() {
        mLocalDataSource = new LocalDataSource(
                Room.databaseBuilder(context, TADatabase.class, TA_DATABASE)
                        .fallbackToDestructiveMigration().build());
    }

    public IEdxEnvironment getEdxEnvironment() {
        return edxEnvironment;
    }

    public Config getConfig() {
        return config;
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(null));
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable, Class<T> cls) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(cls));
    }

    private Observable<EmptyResponse> preEmptyProcess(Observable<BaseResponse<EmptyResponse>> observable) {
        return preProcess(observable, EmptyResponse.class);
    }

    public AppPref getAppPref() {
        return mAppPref;
    }

    public LoginPrefs getLoginPrefs() {
        return loginPrefs;
    }

    public void login(String username, String password, OnResponseCallback<AuthResponse> callback) {
        if (!NetworkUtil.isConnected(context)) {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

        wpClientRetrofit.getAccessToken(username, password, new WordPressRestResponse<WpAuthResponse>() {
            @Override
            public void onSuccess(WpAuthResponse result) {
                loginPrefs.storeWPAuthTokenResponse(result);
                doEdxLogin(username, password, callback);
            }

            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                if (config.isWordpressAuthentication()) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                } else {
                    doEdxLogin(username, password, callback);
                }
            }
        });

    }

    private void doEdxLogin(String username, String password, OnResponseCallback<AuthResponse> callback) {

        new LoginTask(context, username, password) {
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                callback.onSuccess(authResponse);
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

    public void logout() {
        syncAnalytics();
        unscheduleDeleteFeeds();
        edxEnvironment.getRouter().performManualLogout(
                context,
                mDataManager.getEdxEnvironment().getAnalyticsRegistry(),
                mDataManager.getEdxEnvironment().getNotificationDelegate());

        new Thread() {
            @Override
            public void run() {
                mLocalDataSource.clear();
            }
        }.start();
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }

    public void getCollectionConfig(OnResponseCallback<CollectionConfigResponse> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetCollectionConfigTask(context) {
                @Override
                protected void onSuccess(CollectionConfigResponse response) throws Exception {
                    super.onSuccess(response);
                    if (response != null) {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertConfiguration(response);
                            }
                        }.start();
                    }
                    callback.onSuccess(response);
                }

                @Override
                protected void onException(Exception ex) {
                    getCollectionConfigFromLocal(callback, ex);
                }
            }.execute();
        } else {
            getCollectionConfigFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionConfigFromLocal(OnResponseCallback<CollectionConfigResponse> callback, Exception ex) {

        new Task<CollectionConfigResponse>(context) {
            @Override
            public CollectionConfigResponse call() {
                return mLocalDataSource.getConfiguration();
            }

            @Override
            protected void onSuccess(CollectionConfigResponse collectionConfigResponse) throws Exception {
                super.onSuccess(collectionConfigResponse);

                if (collectionConfigResponse == null ||
                        collectionConfigResponse.getCategory() == null ||
                        collectionConfigResponse.getCategory().isEmpty()) {
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(collectionConfigResponse);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

    public void getConfigModifiedDate(OnResponseCallback<ConfigModifiedDateResponse> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetConfigModifiedDateTask(context) {
                @Override
                protected void onSuccess(ConfigModifiedDateResponse configModifiedDateResponse) throws Exception {
                    super.onSuccess(configModifiedDateResponse);
                    callback.onSuccess(configModifiedDateResponse);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getCollectionItems(Long[] listIds, int skip, int take, OnResponseCallback<List<CollectionItemsResponse>> callback) {

        if (NetworkUtil.isConnected(context)) {
            Bundle parameters = new Bundle();
            long[] listIds_long = new long[listIds.length];
            for (int i = 0; i < listIds.length; i++) {
                listIds_long[i] = listIds[i];
            }
            parameters.putLongArray(Constants.KEY_LIST_IDS, listIds_long);
            parameters.putInt(Constants.KEY_SKIP, skip);
            parameters.putInt(Constants.KEY_TAKE, take);
            new GetCollectionItemsTask(context, parameters) {
                @Override
                protected void onSuccess(List<CollectionItemsResponse> collectionItemsList) throws Exception {
                    super.onSuccess(collectionItemsList);
                    if (collectionItemsList != null && !collectionItemsList.isEmpty()) {

                        new Thread() {
                            @Override
                            public void run() {

                                for (CollectionItemsResponse itemsResponse : collectionItemsList) {
                                    if (itemsResponse.getContent() != null) {
                                        for (Content content : itemsResponse.getContent()) {
                                            if (content.getLists() == null) {
                                                content.setLists(new ArrayList<>());
                                            }

                                            Content localContent = mLocalDataSource.getContentById(content.getId());
                                            if (localContent != null && localContent.getLists() != null) {
                                                content.getLists().addAll(localContent.getLists());
                                            }

                                            if (!content.getLists().contains(itemsResponse.getId())) {
                                                content.getLists().add(itemsResponse.getId());
                                            }

                                            mLocalDataSource.insertContent(content);
                                        }
                                    }
                                }
                            }
                        }.start();
                    }
                    callback.onSuccess(collectionItemsList);
                }

                @Override
                protected void onException(Exception ex) {
                    getCollectionItemsFromLocal(listIds, callback, ex);
                }
            }.execute();
        } else {
            getCollectionItemsFromLocal(listIds, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionItemsFromLocal(Long[] listIds, OnResponseCallback<List<CollectionItemsResponse>> callback, Exception ex) {

        new Task<List<CollectionItemsResponse>>(context) {
            @Override
            public List<CollectionItemsResponse> call() {

                List<Content> contents = mLocalDataSource.getContents();
                List<CollectionItemsResponse> responses = new ArrayList<>();
                if (contents != null) {
                    for (Long listId : listIds) {
                        CollectionItemsResponse response = new CollectionItemsResponse();
                        response.setId(listId);
                        response.setContent(new ArrayList<>());
                        responses.add(response);
                    }
                    List<Long> requiredListIds = Arrays.asList(listIds);
                    for (Content content : contents) {
                        if (content.getLists() != null) {
                            for (long listId : content.getLists()) {
                                if (requiredListIds.contains(listId)) {
                                    for (CollectionItemsResponse response : responses) {
                                        if (response.getId() == listId) {
                                            response.getContent().add(content);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return responses;

            }

            @Override
            protected void onSuccess(List<CollectionItemsResponse> collectionItemsResponses) throws Exception {
                super.onSuccess(collectionItemsResponses);

                if (collectionItemsResponses == null || collectionItemsResponses.isEmpty()) {
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(collectionItemsResponses);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

    public void getStateAgendaCount(OnResponseCallback<List<AgendaList>> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetStateAgendaCountTask(context) {
                @Override
                protected void onSuccess(List<AgendaList> agendaLists) throws Exception {
                    super.onSuccess(agendaLists);
                    if (agendaLists != null && !agendaLists.isEmpty()) {
                        loginPrefs.setStateListId(agendaLists.get(0).getList_id());

                        getSources(new OnResponseCallback<List<Source>>() {
                            @Override
                            public void onSuccess(List<Source> data) {
                                for (Source source : data) {
                                    getStateAgendaContent(source.getId(), loginPrefs.getStateListId(), null);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });

                    } else {
                        getSources(new OnResponseCallback<List<Source>>() {
                            @Override
                            public void onSuccess(List<Source> data) {
                                new Thread(){
                                    @Override
                                    public void run() {
                                        for (Source source : data) {
                                            mLocalDataSource.deleteAllStateContents(source.getId());
                                        }
                                    }
                                }.start();
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                    callback.onSuccess(agendaLists);
                }

                @Override
                protected void onException(Exception ex) {
                    getStateAgendaCountFromLocal(callback, ex);
                }
            }.execute();
        } else {
            getStateAgendaCountFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getStateAgendaCountFromLocal(OnResponseCallback<List<AgendaList>> callback, Exception e) {

        new Task<AgendaList>(context) {
            @Override
            public AgendaList call() {

                List<Source> sources = mLocalDataSource.getSources();
                if (sources == null) {
                    return null;
                }

                AgendaList agendaList = new AgendaList();
                agendaList.setList_id(loginPrefs.getStateListId());
                List<AgendaItem> items = new ArrayList<>();
                for (Source source : sources) {
                    List<Content> contents = mLocalDataSource.getStateContents(source.getId());
                    if (contents != null) {
                        AgendaItem item = new AgendaItem();
                        item.setContent_count(contents.size());
                        item.setSource_id(source.getId());
                        item.setSource_name(source.getName());
                        item.setSource_title(source.getTitle());
                        items.add(item);
                    }
                }
                agendaList.setResult(items);

                return agendaList;

            }

            @Override
            protected void onSuccess(AgendaList agendaList) throws Exception {
                super.onSuccess(agendaList);

                if (agendaList == null) {
                    callback.onFailure(e);
                } else {
                    callback.onSuccess(Collections.singletonList(agendaList));
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getMyAgendaCount(OnResponseCallback<AgendaList> callback) {

        if (NetworkUtil.isConnected(context)) {

            getSources(new OnResponseCallback<List<Source>>() {
                @Override
                public void onSuccess(List<Source> data) {
                    for (Source source : data) {
                        getMyAgendaContent(source.getId(), null);
                    }
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

            new GetMyAgendaCountTask(context) {
                @Override
                protected void onSuccess(AgendaList agendaList) throws Exception {
                    super.onSuccess(agendaList);
                    callback.onSuccess(agendaList);
                }

                @Override
                protected void onException(Exception ex) {
                    getMyAgendaCountFromLocal(callback, ex);
                }
            }.execute();
        } else {
            getMyAgendaCountFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getMyAgendaCountFromLocal(OnResponseCallback<AgendaList> callback, Exception e) {

        new Task<AgendaList>(context) {
            @Override
            public AgendaList call() {

                List<Source> sources = mLocalDataSource.getSources();
                if (sources == null) {
                    return null;
                }

                AgendaList agendaList = new AgendaList();
                List<AgendaItem> items = new ArrayList<>();
                for (Source source : sources) {
                    List<Content> contents = mLocalDataSource.getBookmarkedContents(source.getId());
                    if (contents != null) {
                        AgendaItem item = new AgendaItem();
                        item.setContent_count(contents.size());
                        item.setSource_id(source.getId());
                        item.setSource_name(source.getName());
                        item.setSource_title(source.getTitle());
                        items.add(item);
                    }
                }
                agendaList.setResult(items);

                return agendaList;

            }

            @Override
            protected void onSuccess(AgendaList agendaList) throws Exception {
                super.onSuccess(agendaList);

                if (agendaList == null) {
                    callback.onFailure(e);
                } else {
                    callback.onSuccess(agendaList);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getDownloadAgendaCount(List<Source> sources, OnResponseCallback<AgendaList> callback) {

        Map<String, Boolean> receivedSources = new HashMap<>();
        for (Source source : sources) {
            receivedSources.put(source.getName(), false);
        }

        AgendaList agendaList = new AgendaList();
        agendaList.setLevel("Download");
        agendaList.setResult(new ArrayList<>());

        for (Source source : sources) {
            if (source.getType().equalsIgnoreCase(SourceType.edx.name()) ||
                    source.getType().equalsIgnoreCase(SourceType.course.name())) {

                getdownloadedCourseContents(new OnResponseCallback<List<Content>>() {
                    @Override
                    public void onSuccess(List<Content> data) {
                        addContentsToAgendaList(data, agendaList, source);
                        receivedSources.put(source.getName(), true);
                        sendDownloadAgenda(receivedSources, agendaList, callback);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        addContentsToAgendaList(null, agendaList, source);
                        receivedSources.put(source.getName(), true);
                        sendDownloadAgenda(receivedSources, agendaList, callback);
                    }
                });

            } else {

                getdownloadedWPContents(source.getName(), new OnResponseCallback<List<Content>>() {
                    @Override
                    public void onSuccess(List<Content> data) {
                        addContentsToAgendaList(data, agendaList, source);
                        receivedSources.put(source.getName(), true);
                        sendDownloadAgenda(receivedSources, agendaList, callback);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        addContentsToAgendaList(null, agendaList, source);
                        receivedSources.put(source.getName(), true);
                        sendDownloadAgenda(receivedSources, agendaList, callback);
                    }
                });

            }
        }

    }

    private void addContentsToAgendaList(List<Content> contents, AgendaList agendaList, Source source) {

        AgendaItem item = new AgendaItem();
        item.setContent_count(contents == null ? 0 : contents.size());
        item.setSource_name(source.getName());
        item.setSource_title(source.getTitle());
        item.setOrder(source.getOrder());
        agendaList.getResult().add(item);

    }

    private void sendDownloadAgenda(Map<String, Boolean> receivedSources, AgendaList agendaList,
                                    OnResponseCallback<AgendaList> callback) {

        if (receivedSources.values().contains(false)) {
            return;
        }
        callback.onSuccess(agendaList);
    }

    public void getdownloadedCourseContents(OnResponseCallback<List<Content>> callback) {

        new Task<List<Content>>(context) {
            @Override
            public List<Content> call() {
                List<Content> contents = new ArrayList<>();
                List<Long> contentIds;
                try {
                    contentIds = new GetCourseContentsOperation().execute(dbHelper.getDatabase());
                } catch (Exception e) {
                    return contents;
                }

                if (contentIds != null) {
                    for (long id : contentIds) {
                        Content content = mLocalDataSource.getContentById(id);
                        if (content != null) {
                            contents.add(content);
                        }
                    }
                }
                return contents;
            }

            @Override
            protected void onSuccess(List<Content> contents) throws Exception {
                super.onSuccess(contents);

                if (!contents.isEmpty()) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(new TaException("No content downloaded"));
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(new TaException("No content downloaded"));
            }
        }.execute();

    }

    private void getdownloadedWPContents(String sourceName, OnResponseCallback<List<Content>> callback) {

        new Task<List<Content>>(context) {
            @Override
            public List<Content> call() {
                List<Content> contents = new ArrayList<>();
                List<Long> contentIds;
                try {
                    contentIds = new GetWPContentsOperation(sourceName).execute(dbHelper.getDatabase());
                } catch (Exception e) {
                    return contents;
                }

                if (contentIds != null) {
                    for (long id : contentIds) {
                        Content content = mLocalDataSource.getContentById(id);
                        if (content != null) {
                            contents.add(content);
                        }
                    }
                }
                return contents;
            }

            @Override
            protected void onSuccess(List<Content> contents) throws Exception {
                super.onSuccess(contents);

                if (!contents.isEmpty()) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(new TaException("No content downloaded"));
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(new TaException("No content downloaded"));
            }
        }.execute();

    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback, Bundle parameters,
                          @NonNull List<RegistrationOption> blocks) {

        new GetUserAddressTask(context, parameters) {
            @Override
            protected void onSuccess(UserAddressResponse userAddressResponse) throws Exception {
                super.onSuccess(userAddressResponse);
                blocks.clear();
                if (userAddressResponse != null && userAddressResponse.getBlock() != null) {
                    for (Object o : userAddressResponse.getBlock()) {
                        blocks.add(new RegistrationOption(o.toString(), o.toString()));
                    }
                    callback.onSuccess(blocks);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

    public void getCourse(Content content, OnResponseCallback<EnrolledCoursesResponse> callback) {

        if (NetworkUtil.isConnected(context)) {
            new UserEnrollmentCourseTask(context, content.getSource_identity()) {
                @Override
                protected void onSuccess(EnrolledCoursesResponse enrolledCoursesResponse) throws Exception {
                    super.onSuccess(enrolledCoursesResponse);
                    if (enrolledCoursesResponse == null) {
                        callback.onFailure(new TaException("Invalid Course"));
                    } else if (enrolledCoursesResponse.getMode() == null ||
                            enrolledCoursesResponse.getMode().equals("") ||
                            enrolledCoursesResponse.getCourse() == null) {

                        enrolInCourse(content, new OnResponseCallback<ResponseBody>() {
                            @Override
                            public void onSuccess(ResponseBody data) {
                                getCourse(content, callback);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });

                    } else {
                        callback.onSuccess(enrolledCoursesResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getCourseFromLocal(content.getSource_identity(), callback, ex);
                }
            }.execute();
        } else {
            getCourseFromLocal(content.getSource_identity(), callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCourseFromLocal(String courseId, OnResponseCallback<EnrolledCoursesResponse> callback, Exception e) {
        new UserEnrollmentCourseFromCacheTask(context, courseId) {
            @Override
            protected void onSuccess(EnrolledCoursesResponse enrolledCoursesResponse) throws Exception {
                super.onSuccess(enrolledCoursesResponse);
                if (enrolledCoursesResponse == null ||
                        enrolledCoursesResponse.getMode() == null ||
                        enrolledCoursesResponse.getMode().equals("")
                ) {
                    callback.onFailure(e);
                } else {
                    callback.onSuccess(enrolledCoursesResponse);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();
    }

    private void enrolInCourse(Content content, OnResponseCallback<ResponseBody> callback) {

        if (!NetworkUtil.isConnected(context)) {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            return;
        }

        Call<ResponseBody> enrolCall = courseApi.enrolInCourse(content.getSource_identity());
        enrolCall.enqueue(new CourseService.EnrollCallback(context) {
            @Override
            protected void onResponse(@NonNull ResponseBody responseBody) {
                super.onResponse(responseBody);
                callback.onSuccess(responseBody);

                analytic.addMxAnalytics_db(content.getSource_identity(), Action.Enrolled,
                        org.tta.mobile.tta.analytics.analytics_enums.Page.CourseHomePage.name(),
                        org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, content.getSource_identity(),
                        content.getSource_identity(), content.getId());
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                super.onFailure(error);
                callback.onFailure(new TaException(error.getMessage()));

                analytic.addMxAnalytics_db(content.getSource_identity(), Action.EnrolFailed,
                        org.tta.mobile.tta.analytics.analytics_enums.Page.CourseHomePage.name(),
                        org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, content.getSource_identity(),
                        content.getSource_identity(), content.getId());
            }
        });

    }

    public void getTotalLikes(long contentId, OnResponseCallback<TotalLikeResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new TotalLikeTask(context, contentId) {
                @Override
                protected void onSuccess(TotalLikeResponse totalLikeResponse) throws Exception {
                    super.onSuccess(totalLikeResponse);
                    if (totalLikeResponse == null) {
                        callback.onFailure(new TaException("No response for total likes."));
                    } else {
                        callback.onSuccess(totalLikeResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void isLike(long contentId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new IsLikeTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException("No response for is like."));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void isContentMyAgenda(long contentId, long sourceId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new IsContentMyAgendaTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.deleteBookmark(new Bookmark(contentId, sourceId));
                            }
                        }.start();

                        callback.onFailure(new TaException("No response for is content my agenda."));
                    } else {

                        new Thread() {
                            @Override
                            public void run() {
                                if (statusResponse.getStatus()) {
                                    mLocalDataSource.insertBookmark(new Bookmark(contentId, sourceId));
                                } else {
                                    mLocalDataSource.deleteBookmark(new Bookmark(contentId, sourceId));
                                }
                            }
                        }.start();

                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    isLocalContentMyAgenda(contentId, callback);
                }
            }.execute();
        } else {
            isLocalContentMyAgenda(contentId, callback);
        }
    }

    private void isLocalContentMyAgenda(long contentId, OnResponseCallback<StatusResponse> callback) {

        new Task<Bookmark>(context) {
            @Override
            public Bookmark call() {
                return mLocalDataSource.getBookmark(contentId);
            }

            @Override
            protected void onSuccess(Bookmark bookmark) throws Exception {
                super.onSuccess(bookmark);

                if (bookmark == null) {
                    callback.onSuccess(new StatusResponse(false));
                } else {
                    callback.onSuccess(new StatusResponse(true));
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onSuccess(new StatusResponse(false));
            }
        }.execute();

    }

    public void setLike(long contentId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new SetLikeTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void setLikeUsingSourceIdentity(String sourceIdentity, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new SetLikeUsingSourceIdentityTask(context, sourceIdentity) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException("Error occured. Couldn't like."));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void setBookmark(long contentId, long sourceId, OnResponseCallback<BookmarkResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new SetBookmarkTask(context, contentId) {
                @Override
                protected void onSuccess(BookmarkResponse bookmarkResponse) throws Exception {
                    super.onSuccess(bookmarkResponse);
                    if (bookmarkResponse == null) {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    } else {

                        new Thread() {
                            @Override
                            public void run() {
                                if (bookmarkResponse.isIs_active()) {
                                    mLocalDataSource.insertBookmark(new Bookmark(contentId, sourceId));
                                } else {
                                    mLocalDataSource.deleteBookmark(new Bookmark(contentId, sourceId));
                                }
                            }
                        }.start();

                        callback.onSuccess(bookmarkResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getCourseComponent(String courseId, OnResponseCallback<CourseComponent> callback) {

        if (NetworkUtil.isConnected(context)) {

            Call<CourseStructureV1Model> getHierarchyCall = courseApi.getCourseStructureWithoutStale(courseId);
            getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(context, courseId,
                    null, null, null, null) {
                @Override
                protected void onResponse(@NonNull CourseComponent courseComponent) {
                    courseManager.addCourseDataInAppLevelCache(courseId, courseComponent);
                    courseComponent = courseManager.getComponentByCourseId(courseId);
                    if (courseComponent != null) {
                        callback.onSuccess(courseComponent);
                    } else {
                        getLocalCourseComponent(courseId, callback, new TaException("Empty course"));
                    }
                }

                @Override
                protected void onFailure(@NonNull Throwable error) {
                    super.onFailure(error);
                    getLocalCourseComponent(courseId, callback, new TaException(error.getLocalizedMessage()));
                }
            });

        } else {
            getLocalCourseComponent(courseId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getLocalCourseComponent(String courseId, OnResponseCallback<CourseComponent> callback, Exception e) {

        CourseComponent courseComponent = courseManager.getComponentByCourseId(courseId);
        if (courseComponent != null) {
            // Course data exist in app session cache
            callback.onSuccess(courseComponent);
            return;
        }

        new GetCourseDataFromPersistableCacheTask(context, courseId) {
            @Override
            protected void onSuccess(CourseComponent courseComponent) throws Exception {
                super.onSuccess(courseComponent);
                courseComponent = courseManager.getComponentByCourseId(courseId);
                if (courseComponent != null) {
                    callback.onSuccess(courseComponent);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    @NonNull
    private CourseComponent validateCourseComponent(@NonNull CourseComponent courseComponent, String courseId, String courseComponentId) {
        final CourseComponent cached = courseManager.getComponentByIdFromAppLevelCache(
                courseId, courseComponentId);
        courseComponent = cached != null ? cached : courseComponent;
        return courseComponent;
    }

    public void downloadSingle(ScormBlockModel scorm,
                               long contentId,
                               FragmentActivity activity,
                               VideoDownloadHelper.DownloadManagerCallback callback) {
        DownloadEntry de = scorm.getDownloadEntry(edxEnvironment.getStorage());
        if (de != null) {
            de.url = scorm.getDownloadUrl();
            de.title = scorm.getParent().getDisplayName();
            de.content_id = contentId;
            de.lastModified = scorm.getLastModified();
            downloadManager.downloadVideo(de, activity, callback);
        }
    }

    public void downloadMultiple(List<? extends HasDownloadEntry> downloadEntries,
                                 long contentid,
                                 FragmentActivity activity,
                                 VideoDownloadHelper.DownloadManagerCallback callback) {
        downloadManager.downloadVideos(downloadEntries, contentid, activity, callback);
    }

    public void getDownloadedStateForVideoId(String videoId, DataCallback<DownloadEntry.DownloadedState> callback) {
        edxEnvironment.getDatabase().getDownloadedStateForVideoId(videoId, callback);
    }

    public boolean scormNeedsDeletion(ScormBlockModel scorm){
        DownloadEntry entry = scorm.getDownloadEntry(edxEnvironment.getStorage());
        if (entry == null){
            return false;
        }
        if (entry.lastModified == null){
            entry.lastModified = scorm.getLastModified();
            edxEnvironment.getStorage().updateInfoByVideoId(entry.videoId, entry, null);
        }
        return !entry.lastModified.equals(scorm.getLastModified());
    }

    public boolean scormNotDownloaded(ScormBlockModel scorm) {
        return getScormStatus(scorm).equals(ScormStatus.not_downloaded);
    }

    public boolean scormDownloading(ScormBlockModel scorm) {
        return getScormStatus(scorm).equals(ScormStatus.downloading);
    }

    public ScormStatus getScormStatus(ScormBlockModel scorm) {

        DownloadEntry entry = scorm.getDownloadEntry(edxEnvironment.getStorage());
        return getDownloadStatus(entry);

    }

    private ScormStatus getDownloadStatus(DownloadEntry entry) {
        if (entry == null || entry.downloaded.equals(DownloadEntry.DownloadedState.ONLINE)) {
            return ScormStatus.not_downloaded;
        } else if (entry.downloaded.equals(DownloadEntry.DownloadedState.DOWNLOADING)) {
            return ScormStatus.downloading;
        } else if (entry.watched.equals(DownloadEntry.WatchedState.UNWATCHED)) {
            return ScormStatus.downloaded;
        } else if (entry.watched.equals(DownloadEntry.WatchedState.PARTIALLY_WATCHED)) {
            return ScormStatus.watching;
        } else {
            return ScormStatus.watched;
        }
    }

    public void deleteScorm(ScormBlockModel scormBlockModel) {
        DownloadEntry de = scormBlockModel.getDownloadEntry(edxEnvironment.getStorage());
        if (de != null) {
            de.url = scormBlockModel.getDownloadUrl();
            de.title = scormBlockModel.getParent().getDisplayName();
            edxEnvironment.getStorage().removeDownload(de);
        }
    }

    public void getMyAgendaContent(long sourceId, OnResponseCallback<List<Content>> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetMyAgendaContentTask(context, sourceId) {
                @Override
                protected void onSuccess(List<Content> response) throws Exception {
                    super.onSuccess(response);
                    if (response != null && !response.isEmpty()) {

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContents(response);
                                List<Bookmark> bookmarks = new ArrayList<>();
                                for (Content content : response) {
                                    bookmarks.add(new Bookmark(content.getId(), sourceId));
                                }
                                mLocalDataSource.insertBookmarks(bookmarks);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.deleteAllBookmarks(sourceId);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onFailure(new TaException("No content in your agenda"));
                        }
                    }

                }

                @Override
                protected void onException(Exception ex) {
                    if (callback != null) {
                        getMyAgendaContentFromLocal(sourceId, callback, ex);
                    }
                }
            }.execute();
        } else {
            if (callback != null) {
                getMyAgendaContentFromLocal(sourceId, callback,
                        new TaException(context.getString(R.string.no_connection_exception)));
            }
        }
    }

    private void getMyAgendaContentFromLocal(long sourceId, OnResponseCallback<List<Content>> callback, Exception e) {

        new Task<List<Content>>(context) {
            @Override
            public List<Content> call() {
                return mLocalDataSource.getBookmarkedContents(sourceId);
            }

            @Override
            protected void onSuccess(List<Content> contents) throws Exception {
                super.onSuccess(contents);

                if (contents != null && contents.size() > 0) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getStateAgendaContent(long sourceId, long list_id, OnResponseCallback<List<Content>> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetStateAgendaContentTask(context, sourceId, list_id) {
                @Override
                protected void onSuccess(List<Content> response) throws Exception {
                    super.onSuccess(response);
                    if (response != null && !response.isEmpty()) {

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContents(response);
                                List<StateContent> stateContents = new ArrayList<>();
                                for (Content content : response) {
                                    stateContents.add(new StateContent(content.getId(), sourceId));
                                }
                                mLocalDataSource.deleteAllStateContents(sourceId);
                                mLocalDataSource.insertStateContents(stateContents);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onSuccess(response);
                        }

                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.deleteAllStateContents(sourceId);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onFailure(new TaException("No content in your state agenda"));
                        }
                    }

                }

                @Override
                protected void onException(Exception ex) {
                    if (callback != null) {
                        getStateAgendaContentFromLocal(sourceId, callback, ex);
                    }
                }
            }.execute();
        } else {
            if (callback != null) {
                getStateAgendaContentFromLocal(sourceId, callback,
                        new TaException(context.getString(R.string.no_connection_exception)));
            }
        }
    }

    private void getStateAgendaContentFromLocal(long sourceId, OnResponseCallback<List<Content>> callback, Exception e) {

        new Task<List<Content>>(context) {
            @Override
            public List<Content> call() {
                return mLocalDataSource.getStateContents(sourceId);
            }

            @Override
            protected void onSuccess(List<Content> contents) throws Exception {
                super.onSuccess(contents);

                if (contents != null && contents.size() > 0) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getDownloadedContent(String sourceName, OnResponseCallback<List<Content>> callback) {

        if (sourceName.equalsIgnoreCase(SourceName.course.name())) {
            getdownloadedCourseContents(callback);
        } else {
            getdownloadedWPContents(sourceName, callback);
        }
    }

    public void getPostById(long postId, OnResponseCallback<Post> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.getPost(postId, new WordPressRestResponse<Post>() {
                @Override
                public void onSuccess(Post result) {
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onFailure(new TaException("Invalid post"));
                    }
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getPostBySlug(String slug, OnResponseCallback<Post> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.getPostBySlug(slug, new WordPressRestResponse<List<Post>>() {
                @Override
                public void onSuccess(List<Post> result) {
                    if (result == null || result.isEmpty()) {
                        getLocalPostBySlug(slug, callback, new TaException(context.getString(R.string.empty_post_message)));
                    } else {
                        Post post = result.get(0);
                        post.setSlug(UrlUtil.urldecode(post.getSlug()));
                        updateLocalPost(post);
                        callback.onSuccess(post);
                    }
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    getLocalPostBySlug(slug, callback, new TaException(errorResponse.getMessage()));
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            getLocalPostBySlug(slug, callback,
                    new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getLocalPostBySlug(String slug, OnResponseCallback<Post> callback, Exception e) {

        Post post = db_commands.getPostBySlug(slug);
        if (post == null || post.getSlug() == null || post.getSlug().equals("")) {
            callback.onFailure(e);
        } else {
            callback.onSuccess(post);
        }

    }

    private void updateLocalPost(Post post) {
        db_commands.updatePostCache(Collections.singletonList(post));
    }

    public void getCommentsByPost(long postId, int take, int page, OnResponseCallback<List<Comment>> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.getCommentsByPost(postId, take, page, loginPrefs.getWPUserId(),
                    new WordPressRestResponse<List<Comment>>() {
                        @Override
                        public void onSuccess(List<Comment> result) {
                            if (result == null) {
                                result = new ArrayList<>();
                            }
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(HttpServerErrorResponse errorResponse) {
                            callback.onFailure(new TaException(errorResponse.getMessage()));
                        }
                    });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getRepliesOnComment(long postId, long commentId, OnResponseCallback<List<Comment>> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.getRepliesOnComment(postId, commentId, loginPrefs.getWPUserId(),
                    new WordPressRestResponse<List<Comment>>() {
                        @Override
                        public void onSuccess(List<Comment> result) {
                            if (result == null) {
                                result = new ArrayList<>();
                            }
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(HttpServerErrorResponse errorResponse) {
                            callback.onFailure(new TaException(errorResponse.getMessage()));
                        }
                    });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void downloadPost(Post post, long contentId, String category_id, String category_name,
                             FragmentActivity activity,
                             VideoDownloadHelper.DownloadManagerCallback callback) {

        DownloadEntry videoData = new DownloadEntry();
        videoData.setDownloadEntryForPost(contentId, category_id, category_name, post);
        downloadManager.downloadVideo(videoData, activity, callback);

    }

    public void deletePost(Post post) {

        DownloadEntry entry = edxEnvironment.getStorage().getPostVideo(String.valueOf(post.getId()));
        if (entry != null)
            edxEnvironment.getStorage().removeDownload(entry);

    }

    public ScormStatus getPostDownloadStatus(Post post) {

        DownloadEntry entry = edxEnvironment.getStorage().getPostVideo(String.valueOf(post.getId()));
        return getDownloadStatus(entry);

    }

    public void addComment(String comment, int commentParentId, long postId, OnResponseCallback<Comment> callback) {
        if (loginPrefs.getWPCurrentUserProfile() == null || loginPrefs.getWPCurrentUserProfile().id == null) {
            callback.onFailure(new TaException(context.getString(R.string.not_authenticated_to_comment)));
            return;
        }

        String ua = new WebView(context).getSettings().getUserAgentString();
        CustomComment obj = new CustomComment();
        obj.author = loginPrefs.getWPCurrentUserProfile().id;
        //obj.author_ip=ip;
        obj.author_url = "";
        obj.author_user_agent = ua;
        obj.content = comment;
        obj.date = DateUtil.getCurrentDateForServerLocal();
        obj.date_gmt = DateUtil.getCurrentDateForServerGMT();
        obj.parent = commentParentId;
        obj.post = postId;

        addComment(obj, callback);
    }

    private void addComment(CustomComment comment, OnResponseCallback<Comment> callback) {
        wpClientRetrofit.createComment(comment, new WordPressRestResponse<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                callback.onFailure(new TaException(errorResponse.getMessage()));
            }
        });
    }

    public void setWpProfileCache() {
        wpClientRetrofit.getUserMe(new WordPressRestResponse<User>() {
            @Override
            public void onSuccess(User result) {
                WPProfileModel model = new WPProfileModel();
                model.name = result.getName();
                model.username = result.getUsername();
                model.id = result.getId();
                if (result.getRoles() != null && result.getRoles().size() > 0)
                    model.roles = result.getRoles();
                loginPrefs.setWPCurrentUserProfileInCache(model);
            }

            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                if (config.isWordpressAuthentication() &&
                        !NetworkUtil.isLimitedAcess(errorResponse) && NetworkUtil.isUnauthorize(errorResponse)) {
                    logout();
                    Toast.makeText(context, context.getString(R.string.session_expire), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public DownloadEntry getDownloadedVideo(Post post, long contentId, String categoryId, String categoryName) {
        DownloadEntry videoData = new DownloadEntry();
        videoData.setDownloadEntryForPost(contentId, categoryId, categoryName, post);

        return edxEnvironment.getStorage().getPostVideo(videoData.videoId, videoData.url);

    }

    public void getSearchFilter(OnResponseCallback<SearchFilter> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetSearchFilterTask(context) {
                @Override
                protected void onSuccess(SearchFilter searchFilter) throws Exception {
                    super.onSuccess(searchFilter);
                    if (searchFilter == null) {
                        callback.onFailure(new TaException("Cannot fetch filters"));
                    } else {
                        List<FilterSection> tempSections = new ArrayList<>();
                        for (FilterSection section : searchFilter.getResult()) {
                            if (section.getTags() == null || section.getTags().isEmpty()) {
                                tempSections.add(section);
                            }
                        }
                        for (FilterSection section : tempSections) {
                            searchFilter.getResult().remove(section);
                        }
                        Collections.sort(searchFilter.getResult());
                        loginPrefs.setSearchFilter(searchFilter);
                        callback.onSuccess(searchFilter);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    SearchFilter searchFilter = loginPrefs.getSearchFilter();
                    if (searchFilter == null) {
                        callback.onFailure(ex);
                    } else {
                        callback.onSuccess(searchFilter);
                    }
                }
            }.execute();
        } else {
            SearchFilter searchFilter = loginPrefs.getSearchFilter();
            if (searchFilter == null) {
                callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            } else {
                callback.onSuccess(searchFilter);
            }
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCategoryFromLocal(long sourceId, OnResponseCallback<Category> callback) {

        new Task<Category>(context) {
            @Override
            public Category call() {
                return mLocalDataSource.getCategoryBySourceId(sourceId);
            }

            @Override
            protected void onSuccess(Category category) throws Exception {
                super.onSuccess(category);

                if (category == null) {
                    callback.onFailure(new TaException("Category not found."));
                } else {
                    callback.onSuccess(category);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(new TaException("Category not found."));
            }
        }.execute();

    }

    public void getContentListsFromLocal(long categoryId, String mode, OnResponseCallback<List<ContentList>> callback) {

        new Task<List<ContentList>>(context) {
            @Override
            public List<ContentList> call() {
                return mLocalDataSource.getContentListsByCategoryIdAndMode(categoryId, mode);
            }

            @Override
            protected void onSuccess(List<ContentList> contentLists) throws Exception {
                super.onSuccess(contentLists);

                if (contentLists == null || contentLists.isEmpty()) {
                    callback.onFailure(new TaException("Content lists not found."));
                } else {
                    Collections.sort(contentLists);
                    callback.onSuccess(contentLists);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(new TaException("Content lists not found."));
            }
        }.execute();

    }

    public void search(int take, int skip, boolean isPriority, long listId, String searchText,
                       List<FilterSection> sections, long sourceId, OnResponseCallback<List<Content>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new SearchTask(context, take, skip, isPriority, listId, searchText, sourceId, sections) {
                @Override
                protected void onSuccess(List<Content> contents) throws Exception {
                    super.onSuccess(contents);
                    if (contents == null) {
                        contents = new ArrayList<>();
                    }

                    if (!contents.isEmpty()) {
                        List<Content> finalContents = contents;
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContents(finalContents);
                            }
                        }.start();
                    }

                    callback.onSuccess(contents);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            searchLocalContent(sourceId, take, skip, callback,
                    new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void searchLocalContent(long sourceId, int take, int skip, OnResponseCallback<List<Content>> callback, Exception e){

        new Task<List<Content>>(context) {
            @Override
            public List<Content> call() {
                if (sourceId == 0) {
                    return mLocalDataSource.getContents(take, skip);
                } else {
                    return mLocalDataSource.getContentsBySourceId(sourceId, take, skip);
                }
            }

            @Override
            protected void onSuccess(List<Content> contents) throws Exception {
                super.onSuccess(contents);

                if (contents == null) {
                    contents = new ArrayList<>();
                }
                callback.onSuccess(contents);
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void searchPeople(int take, int skip, String searchText, OnResponseCallback<List<SuggestedUser>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new SearchPeopleTask(context, take, skip, searchText) {
                @Override
                protected void onSuccess(List<SuggestedUser> users) throws Exception {
                    super.onSuccess(users);
                    if (users == null) {
                        users = new ArrayList<>();
                    }

                    callback.onSuccess(users);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void submitFeedback(String msg, OnResponseCallback<FeedbackResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_USERNAME, loginPrefs.getUsername());
            parameters.putString(Constants.KEY_FEEDBACK, msg);
            parameters.putString(Constants.KEY_DEVICE_INFO,
                    "API Level:" + Build.VERSION.RELEASE + "  Device:" + Build.DEVICE + "  Model no:" + Build.MODEL + "  Product:" + Build.PRODUCT);

            new SubmitFeedbackTask(context, parameters) {
                @Override
                protected void onSuccess(FeedbackResponse feedbackResponse) throws Exception {
                    super.onSuccess(feedbackResponse);
                    callback.onSuccess(feedbackResponse);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void changePassword(String oldPass, String newPass, OnResponseCallback<ChangePasswordResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_OLD_PASSWORD, oldPass);
            parameters.putString(Constants.KEY_NEW_PASSWORD, newPass);
            parameters.putString(Constants.KEY_USERNAME, loginPrefs.getUsername());

            new ChangePasswordTask(context, parameters) {
                @Override
                protected void onSuccess(ChangePasswordResponse changePasswordResponse) throws Exception {
                    super.onSuccess(changePasswordResponse);

                    if (changePasswordResponse != null && changePasswordResponse.isSuccess()) {
                        edxEnvironment.getRouter().resetAuthForChangePassword(context,
                                edxEnvironment.getAnalyticsRegistry(), edxEnvironment.getNotificationDelegate());
                        login(loginPrefs.getUsername(), newPass, new OnResponseCallback<AuthResponse>() {
                            @Override
                            public void onSuccess(AuthResponse data) {
                                callback.onSuccess(changePasswordResponse);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context,
                                        context.getString(R.string.password_changed_login),
                                        Toast.LENGTH_LONG).show();
                                logout();
                            }
                        });
                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.correct_old_password)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getAccount(OnResponseCallback<Account> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetAccountTask(context, loginPrefs.getUsername()) {
                @Override
                protected void onSuccess(Account account) throws Exception {
                    super.onSuccess(account);
                    if (account != null) {
                        loginPrefs.setProfileImage(loginPrefs.getUsername(), account.getProfileImage());

                        getProfile(new OnResponseCallback<ProfileModel>() {
                            @Override
                            public void onSuccess(ProfileModel data) {
                                loginPrefs.setCurrentUserProfileInCache(data);
                                callback.onSuccess(account);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onSuccess(account);
                            }
                        });

                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.unable_to_get_account)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void updateProfile(Bundle parameters, OnResponseCallback<UpdateMyProfileResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            new UpdateMyProfileTask(context, parameters, loginPrefs.getUsername()) {
                @Override
                protected void onSuccess(UpdateMyProfileResponse updateMyProfileResponse) throws Exception {
                    super.onSuccess(updateMyProfileResponse);

                    if (updateMyProfileResponse == null) {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                        return;
                    }

                    //for cache consistency
                    ProfileModel profileModel = new ProfileModel();
                    profileModel.name = updateMyProfileResponse.getName();
                    profileModel.email = updateMyProfileResponse.getEmail();
                    profileModel.gender = updateMyProfileResponse.getGender();

                    profileModel.title = updateMyProfileResponse.getTitle();
                    profileModel.classes_taught = updateMyProfileResponse.getClasses_taught();
                    profileModel.state = updateMyProfileResponse.getState();
                    profileModel.district = updateMyProfileResponse.getDistrict();
                    profileModel.block = updateMyProfileResponse.getBlock();
                    profileModel.pmis_code = updateMyProfileResponse.getPMIS_code();
                    profileModel.diet_code = updateMyProfileResponse.getDIETCode();
                    profileModel.setTagLabel(updateMyProfileResponse.getTagLabel());
                    profileModel.setFollowers(updateMyProfileResponse.getFollowers());
                    profileModel.setFollowing(updateMyProfileResponse.getFollowing());

                    loginPrefs.setCurrentUserProfileInCache(profileModel);
                    loginPrefs.removeMxProfilePageCache();

                    callback.onSuccess(updateMyProfileResponse);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void updateProfileImage(@NonNull Uri uri, @NonNull Rect cropRect,
                                   OnResponseCallback<ProfileImage> callback) {

        if (NetworkUtil.isConnected(context)) {

            new SetAccountImageTask(context, loginPrefs.getUsername(), uri, cropRect) {
                @Override
                protected void onSuccess(Void response) throws Exception {
                    super.onSuccess(response);

                    getAccount(new OnResponseCallback<Account>() {
                        @Override
                        public void onSuccess(Account data) {
                            loginPrefs.setProfileImage(loginPrefs.getUsername(), data.getProfileImage());
                            callback.onSuccess(data.getProfileImage());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getProfile(OnResponseCallback<ProfileModel> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetProfileTask(context) {
                @Override
                protected void onSuccess(ProfileModel profileModel) throws Exception {
                    super.onSuccess(profileModel);
                    callback.onSuccess(profileModel);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            getProfileFromLocal(new TaException(context.getString(R.string.no_connection_exception)), callback);
        }

    }

    private void getProfileFromLocal(Exception e, OnResponseCallback<ProfileModel> callback) {
        ProfileModel model = loginPrefs.getCurrentUserProfile();
        if (model != null) {
            callback.onSuccess(model);
        } else {
            callback.onFailure(e);
        }
    }

    public void getCertificateStatus(String courseId, OnResponseCallback<CertificateStatusResponse> callback) {

        getCertificateFromLocal(courseId, new OnResponseCallback<Certificate>() {
            @Override
            public void onSuccess(Certificate data) {
                CertificateStatusResponse response = new CertificateStatusResponse();
                response.setStatus(CertificateStatus.GENERATED.name());
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(Exception e) {

                if (NetworkUtil.isConnected(context)) {

                    new GetCertificateStatusTask(context, courseId) {
                        @Override
                        protected void onSuccess(CertificateStatusResponse certificateStatusResponse) throws Exception {
                            super.onSuccess(certificateStatusResponse);
                            if (certificateStatusResponse != null) {
                                callback.onSuccess(certificateStatusResponse);
                            } else {
                                callback.onFailure(new TaException("Status of certificate could not be fetched"));
                            }
                        }

                        @Override
                        protected void onException(Exception ex) {
                            callback.onFailure(ex);
                        }
                    }.execute();

                } else {
                    callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
                }

            }
        }, null);

    }

    public void getCertificate(String courseId, OnResponseCallback<Certificate> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetCertificateTask(context, courseId) {
                @Override
                protected void onSuccess(MyCertificatesResponse myCertificatesResponse) throws Exception {
                    super.onSuccess(myCertificatesResponse);
                    if (myCertificatesResponse == null || myCertificatesResponse.getCertificates() == null ||
                            myCertificatesResponse.getCertificates().isEmpty()) {
                        getCertificateFromLocal(courseId, callback, new TaException(context.getString(R.string.certificate_not_available)));
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                Certificate certificate = myCertificatesResponse.getCertificates().get(0);
                                if (certificate.getUsername() == null) {
                                    certificate.setUsername(loginPrefs.getUsername());
                                }
                                mLocalDataSource.insertCertificate(certificate);
                            }
                        }.start();

                        callback.onSuccess(myCertificatesResponse.getCertificates().get(0));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getCertificateFromLocal(courseId, callback, ex);
                }
            }.execute();

        } else {
            getCertificateFromLocal(courseId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCertificateFromLocal(String courseId, OnResponseCallback<Certificate> callback, Exception e) {

        new Task<Certificate>(context) {
            @Override
            public Certificate call() {
                return mLocalDataSource.getCertificate(courseId, loginPrefs.getUsername());
            }

            @Override
            protected void onSuccess(Certificate certificate) throws Exception {
                super.onSuccess(certificate);

                if (certificate != null) {
                    callback.onSuccess(certificate);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getMyCertificates(OnResponseCallback<List<Certificate>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetMyCertificatesTask(context) {
                @Override
                protected void onSuccess(MyCertificatesResponse myCertificatesResponse) throws Exception {
                    super.onSuccess(myCertificatesResponse);
                    if (myCertificatesResponse == null || myCertificatesResponse.getCertificates() == null) {
                        getMyCertificatesFromLocal(callback, new TaException("Certificates not available"));
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                List<Certificate> certificates = myCertificatesResponse.getCertificates();
                                for (Certificate certificate : certificates) {
                                    if (certificate.getUsername() == null) {
                                        certificate.setUsername(loginPrefs.getUsername());
                                    }
                                }
                                mLocalDataSource.insertCertificates(certificates);
                            }
                        }.start();

                        callback.onSuccess(myCertificatesResponse.getCertificates());
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getMyCertificatesFromLocal(callback, new TaException("Certificates not available"));
                }
            }.execute();

        } else {
            getMyCertificatesFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getMyCertificatesFromLocal(OnResponseCallback<List<Certificate>> callback, Exception e) {

        new Task<List<Certificate>>(context) {
            @Override
            public List<Certificate> call() {
                return mLocalDataSource.getAllCertificates(loginPrefs.getUsername());
            }

            @Override
            protected void onSuccess(List<Certificate> certificates) throws Exception {
                super.onSuccess(certificates);

                if (certificates != null) {
                    callback.onSuccess(certificates);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void generateCertificate(String courseId, OnResponseCallback<CertificateStatusResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GenerateCertificateTask(context, courseId) {
                @Override
                protected void onSuccess(CertificateStatusResponse certificateStatusResponse) throws Exception {
                    super.onSuccess(certificateStatusResponse);
                    if (certificateStatusResponse != null) {
                        callback.onSuccess(certificateStatusResponse);
                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.certificate_not_generated)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getContent(long contentId, OnResponseCallback<Content> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetContentTask(context, contentId) {
                @Override
                protected void onSuccess(Content content) throws Exception {
                    super.onSuccess(content);
                    if (content != null && content.getDetail() == null) {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertOrIgnoreContent(content);
                            }
                        }.start();

                        callback.onSuccess(content);
                    } else {
                        getContentFromLocal(contentId, callback, new TaException("Content not found"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getContentFromLocal(contentId, callback, ex);
                }
            }.execute();

        } else {
            getContentFromLocal(contentId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getContentFromLocal(long contentId, OnResponseCallback<Content> callback, Exception e) {

        new Task<Content>(context) {
            @Override
            public Content call() {
                return mLocalDataSource.getContentById(contentId);
            }

            @Override
            protected void onSuccess(Content content) throws Exception {
                super.onSuccess(content);

                if (content != null) {
                    callback.onSuccess(content);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getSuggestedUsers(int take, int skip, OnResponseCallback<List<SuggestedUser>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetSuggestedUsersTask(context, take, skip) {
                @Override
                protected void onSuccess(List<SuggestedUser> suggestedUsers) throws Exception {
                    super.onSuccess(suggestedUsers);
                    if (suggestedUsers == null || suggestedUsers.isEmpty()) {
                        callback.onFailure(new TaException("No suggested users"));
                    } else {
                        List<SuggestedUser> emptyUsers = new ArrayList<>();
                        for (SuggestedUser user : suggestedUsers) {
                            if (user.getUsername() == null) {
                                emptyUsers.add(user);
                            }
                        }
                        for (SuggestedUser user : emptyUsers) {
                            suggestedUsers.remove(user);
                        }
                        if (!suggestedUsers.isEmpty()) {
                            callback.onSuccess(suggestedUsers);
                        } else {
                            callback.onFailure(new TaException("No suggested users"));
                        }
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void followUnfollowUser(String username, OnResponseCallback<StatusResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            new FollowUserTask(context, username) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getDiscussionTopics(String courseId, OnResponseCallback<List<DiscussionTopicDepth>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetDiscussionTopicsTask(context, courseId) {
                @Override
                protected void onSuccess(CourseTopics courseTopics) throws Exception {
                    super.onSuccess(courseTopics);
                    if (courseTopics == null ||
                            (courseTopics.getCoursewareTopics() == null && courseTopics.getNonCoursewareTopics() == null)) {
                        callback.onFailure(new TaException("No discussion topics available"));
                    } else {
                        List<DiscussionTopic> allTopics = new ArrayList<>();
                        if (courseTopics.getNonCoursewareTopics() != null) {
                            allTopics.addAll(courseTopics.getNonCoursewareTopics());
                        }
                        if (courseTopics.getCoursewareTopics() != null) {
                            allTopics.addAll(courseTopics.getCoursewareTopics());
                        }
                        if (!allTopics.isEmpty()) {
                            List<DiscussionTopicDepth> allTopicsWithDepth =
                                    DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                            callback.onSuccess(allTopicsWithDepth);
                        } else {
                            callback.onFailure(new TaException("No discussion topics available"));
                        }
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getDiscussionThreads(String courseId, List<String> topicIds, String view,
                                     String orderBy, int take, int page, List<String> requestedFields,
                                     OnResponseCallback<List<DiscussionThread>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetDiscussionThreadsTask(context, courseId, topicIds, view, orderBy, take, page, requestedFields) {
                @Override
                protected void onSuccess(Page<DiscussionThread> discussionThreadPage) throws Exception {
                    super.onSuccess(discussionThreadPage);
                    if (discussionThreadPage == null || discussionThreadPage.getResults().isEmpty()) {
                        callback.onFailure(new TaException("No discussion threads available"));
                    } else {
                        callback.onSuccess(discussionThreadPage.getResults());
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getDiscussionThread(String threadId, OnResponseCallback<DiscussionThread> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetDiscussionThreadTask(context, threadId) {
                @Override
                protected void onSuccess(DiscussionThread thread) throws Exception {
                    super.onSuccess(thread);
                    if (thread == null) {
                        callback.onFailure(new TaException("Unable to fetch discussion thread"));
                    } else {
                        callback.onSuccess(thread);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getThreadComments(String threadId, int take, int page, List<String> requestedFields, boolean isQuestionType,
                                  OnResponseCallback<List<DiscussionComment>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetThreadCommentsTask(context, threadId, take, page, requestedFields, isQuestionType) {
                @Override
                protected void onSuccess(Page<DiscussionComment> discussionCommentPage) throws Exception {
                    super.onSuccess(discussionCommentPage);
                    if (discussionCommentPage == null || discussionCommentPage.getResults().isEmpty()) {
                        callback.onFailure(new TaException("No comments available"));
                    } else {
                        callback.onSuccess(discussionCommentPage.getResults());
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCommentReplies(String commentId, int take, int page, List<String> requestedFields,
                                  OnResponseCallback<List<DiscussionComment>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetCommentRepliesTask(context, commentId, take, page, requestedFields) {
                @Override
                protected void onSuccess(Page<DiscussionComment> discussionCommentPage) throws Exception {
                    super.onSuccess(discussionCommentPage);
                    if (discussionCommentPage == null || discussionCommentPage.getResults().isEmpty()) {
                        callback.onFailure(new TaException("No replies available"));
                    } else {
                        callback.onSuccess(discussionCommentPage.getResults());
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void createDiscussionComment(String threadId, String comment, String parentCommentId,
                                        OnResponseCallback<DiscussionComment> callback) {

        if (NetworkUtil.isConnected(context)) {

            new CreateDiscussionCommentTask(context, threadId, comment, parentCommentId) {
                @Override
                protected void onSuccess(DiscussionComment comment) throws Exception {
                    super.onSuccess(comment);
                    if (comment != null) {
                        callback.onSuccess(comment);
                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void likeDiscussionThread(String threadId, boolean liked, OnResponseCallback<DiscussionThread> callback) {

        if (NetworkUtil.isConnected(context)) {

            new LikeDiscussionThreadTask(context, threadId, liked) {
                @Override
                protected void onSuccess(DiscussionThread thread) throws Exception {
                    super.onSuccess(thread);
                    if (thread != null) {
                        callback.onSuccess(thread);
                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void likeDiscussionComment(String commentId, boolean liked, OnResponseCallback<DiscussionComment> callback) {

        if (NetworkUtil.isConnected(context)) {

            new LikeDiscussionCommentTask(context, commentId, liked) {
                @Override
                protected void onSuccess(DiscussionComment comment) throws Exception {
                    super.onSuccess(comment);
                    if (comment != null) {
                        callback.onSuccess(comment);
                    } else {
                        callback.onFailure(new TaException(context.getString(R.string.action_not_completed)));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void findContentForAssistant(String searchText, List<String> tags, OnResponseCallback<List<Content>> callback) {
        if (NetworkUtil.isConnected(context)) {

            edxDataManager.getTaAPI().assistantSearch(searchText, tags).enqueue(new Callback<List<Content>>() {
                @Override
                protected void onResponse(@NonNull List<Content> responseBody) {
                    if (callback != null)
                        callback.onSuccess(responseBody);
                }

                @Override
                protected void onFailure(@NonNull Throwable error) {
                    super.onFailure(error);
                    if (callback != null)
                        callback.onFailure(new TaException(error.getMessage()));

                }
            });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getSources(OnResponseCallback<List<Source>> callback) {

        new Task<List<Source>>(context) {
            @Override
            public List<Source> call() {
                return mLocalDataSource.getSources();
            }

            @Override
            protected void onSuccess(List<Source> sources) throws Exception {
                super.onSuccess(sources);

                if (sources == null || sources.isEmpty()) {
                    callback.onFailure(new TaException("No sources available"));
                } else {
                    callback.onSuccess(sources);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(new TaException("No sources available"));
            }
        }.execute();

    }

    private void syncNotifications() {
        if (loginPrefs != null && loginPrefs.isLoggedIn()) {
            createNotifications();
            updateNotifications();
        }
    }

    private void updateNotifications() {

        if (NetworkUtil.isConnected(context)) {

            new Task<List<Notification>>(context) {
                @Override
                public List<Notification> call() {
                    return mLocalDataSource.getAllUnupdatedNotifications(loginPrefs.getUsername());
                }

                @Override
                protected void onSuccess(List<Notification> notifications) throws Exception {
                    super.onSuccess(notifications);

                    if (notifications != null && !notifications.isEmpty()) {
                        List<Long> notificationIds = new ArrayList<>();
                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        new UpdateNotificationsTask(context, notificationIds) {
                            @Override
                            protected void onSuccess(CountResponse countResponse) throws Exception {
                                super.onSuccess(countResponse);

                                for (Notification notification : notifications) {
                                    notification.setUpdated(true);
                                }
                                updateNotificationsInLocal(notifications);
                            }

                            @Override
                            protected void onException(Exception ex) {

                            }
                        }.execute();
                    }
                }

                @Override
                protected void onException(Exception ex) {

                }
            }.execute();

        }

    }

    public void updateNotificationsInLocal(List<Notification> notifications) {

        new Thread() {
            @Override
            public void run() {

                for (Notification notification : notifications) {
                    Notification localNotification = null;
                    if (notification.getLocal_id() != 0) {
                        localNotification = mLocalDataSource.getNotificationByLocalId(
                                loginPrefs.getUsername(), notification.getLocal_id());
                    }

                    if (localNotification == null) {
                        if (notification.getId() != 0) {
                            localNotification = mLocalDataSource.getNotificationById(
                                    loginPrefs.getUsername(), notification.getId());
                        } else {
                            localNotification = mLocalDataSource.getNotificationByCreatedTime(
                                    loginPrefs.getUsername(), notification.getCreated_time());
                        }
                    }

                    if (localNotification == null) {
                        addNotification(notification);
                    } else {
                        localNotification.set(notification);
                        mLocalDataSource.updateNotification(localNotification);
                    }
                }

            }
        }.start();

    }

    public void getNotifications(int take, int skip, OnResponseCallback<List<Notification>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetNotificationsTask(context, take, skip) {
                @Override
                protected void onSuccess(List<Notification> notifications) throws Exception {
                    super.onSuccess(notifications);

                    if (notifications != null) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                mLocalDataSource.insertNotifications(notifications);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                getNotificationsFromLocal(take, skip, callback, new TaException("Notifications not available"));
                            }
                        }.execute();

                    } else {
                        getNotificationsFromLocal(take, skip, callback, new TaException("Notifications not available"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getNotificationsFromLocal(take, skip, callback, ex);
                }
            }.execute();

        } else {
            getNotificationsFromLocal(take, skip, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getNotificationsFromLocal(int take, int skip, OnResponseCallback<List<Notification>> callback, Exception e) {

        new Task<List<Notification>>(context) {
            @Override
            public List<Notification> call() {
                return mLocalDataSource.getAllNotificationsInPage(loginPrefs.getUsername(), take, skip);
            }

            @Override
            protected void onSuccess(List<Notification> notifications) throws Exception {
                super.onSuccess(notifications);

                if (notifications == null || notifications.isEmpty()) {
                    callback.onFailure(e);
                } else {
                    callback.onSuccess(notifications);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    private void createNotifications() {

        if (NetworkUtil.isConnected(context)) {

            new Task<List<Notification>>(context) {
                @Override
                public List<Notification> call() {
                    return mLocalDataSource.getAllUncreatedNotifications(loginPrefs.getUsername());
                }

                @Override
                protected void onSuccess(List<Notification> notifications) throws Exception {
                    super.onSuccess(notifications);

                    if (notifications != null && !notifications.isEmpty()) {

                        new CreateNotificationsTask(context, notifications) {
                            @Override
                            protected void onSuccess(List<Notification> notifications) throws Exception {
                                super.onSuccess(notifications);
                                if (notifications != null && !notifications.isEmpty()) {
                                    updateNotificationsInLocal(notifications);
                                }
                            }

                            @Override
                            protected void onException(Exception ex) {

                            }
                        }.execute();

                    }
                }

                @Override
                protected void onException(Exception ex) {

                }
            }.execute();

        }

    }

    public void addNotification(Notification n) {
        new Thread() {
            @Override
            public void run() {
                mLocalDataSource.insertNotification(n);
            }
        }.start();
    }

    public void onAppStartOrClose() {
        syncAnalytics();
        syncNotifications();
    }

    private void syncAnalytics() {

        if (loginPrefs != null && loginPrefs.isLoggedIn()) {
            try {
                analytic.syncAnalytics();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void getFeeds(int skip, OnResponseCallback<List<Feed>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetFeedsTask(context, skip) {
                @Override
                protected void onSuccess(List<Feed> feeds) throws Exception {
                    super.onSuccess(feeds);

                    if (feeds != null && !feeds.isEmpty()) {

                        List<Feed> removables = new ArrayList<>();
                        for (Feed feed: feeds){
                            if (feed.getMeta_data() == null){
                                removables.add(feed);
                            }
                        }

                        for (Feed feed: removables){
                            feeds.remove(feed);
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                for (Feed feed : feeds) {
                                    feed.setUsername(loginPrefs.getUsername());
                                }
                                mLocalDataSource.insertFeeds(feeds);
                            }
                        }.start();

                        callback.onSuccess(feeds);
                    } else {
                        callback.onFailure(new TaException("No feeds available"));
                    }

                }

                @Override
                protected void onException(Exception ex) {
                    getFeedsFromLocal(10, skip, callback, ex);
                }
            }.execute();

        } else {
            getFeedsFromLocal(10, skip, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getFeedsFromLocal(int take, int skip, OnResponseCallback<List<Feed>> callback, Exception e) {

        new Task<List<Feed>>(context) {
            @Override
            public List<Feed> call() {
                return mLocalDataSource.getFeeds(loginPrefs.getUsername(), take, skip);
            }

            @Override
            protected void onSuccess(List<Feed> feeds) throws Exception {
                super.onSuccess(feeds);

                if (feeds != null && !feeds.isEmpty()) {
                    callback.onSuccess(feeds);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getContentFromSourceIdentity(String sourceIdentity, OnResponseCallback<Content> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetContentFromSourceIdentityTask(context, sourceIdentity) {
                @Override
                protected void onSuccess(Content content) throws Exception {
                    super.onSuccess(content);

                    if (content == null || content.getId() == 0) {
                        getLocalContentFromSourceIdentity(sourceIdentity, callback,
                                new TaException(context.getString(R.string.content_not_found)));
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertOrIgnoreContent(content);
                            }
                        }.start();
                        callback.onSuccess(content);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getLocalContentFromSourceIdentity(sourceIdentity, callback, ex);
                }
            }.execute();

        } else {
            getLocalContentFromSourceIdentity(sourceIdentity, callback,
                    new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getLocalContentFromSourceIdentity(String sourceIdentity, OnResponseCallback<Content> callback, Exception e) {

        new Task<Content>(context) {
            @Override
            public Content call() {
                return mLocalDataSource.getContentBySourceIdentity(sourceIdentity);
            }

            @Override
            protected void onSuccess(Content content) throws Exception {
                super.onSuccess(content);

                if (content != null) {
                    callback.onSuccess(content);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getFeedFeatureList(OnResponseCallback<List<Content>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new Task<ContentList>(context) {
                @Override
                public ContentList call() {
                    List<ContentList> contentLists = mLocalDataSource.getContentListsByRootCategory("feed");
                    if (contentLists == null || contentLists.isEmpty()) {
                        return null;
                    } else {
                        return contentLists.get(0);
                    }
                }

                @Override
                protected void onSuccess(ContentList contentList) throws Exception {
                    super.onSuccess(contentList);

                    if (contentList != null) {
                        getCollectionItems(new Long[]{contentList.getId()}, 0, 5,
                                new OnResponseCallback<List<CollectionItemsResponse>>() {
                                    @Override
                                    public void onSuccess(List<CollectionItemsResponse> data) {
                                        if (data == null || data.isEmpty() ||
                                                data.get(0).getContent() == null ||
                                                data.get(0).getContent().isEmpty()
                                        ) {
                                            callback.onFailure(new TaException("Featured contents not available"));
                                        } else {
                                            callback.onSuccess(data.get(0).getContent());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                    } else {
                        callback.onFailure(new TaException("Feature list not available"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(new TaException("Feature list not available"));
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void setUserContent(List<ContentStatus> statuses, OnResponseCallback<List<ContentStatus>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new SetUserContentTask(context, statuses) {
                @Override
                protected void onSuccess(List<ContentStatus> contentStatuses) throws Exception {
                    super.onSuccess(contentStatuses);

                    if (contentStatuses != null && contentStatuses.size() == statuses.size()) {

                        List<ContentStatus> finalStatuses = new ArrayList<>();
                        for (ContentStatus status : contentStatuses) {
                            if (status.getError() == null) {
                                status.setUsername(loginPrefs.getUsername());
                                finalStatuses.add(status);
                            }
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContentStatuses(finalStatuses);
                            }
                        }.start();

                        callback.onSuccess(finalStatuses);

                    } else {
                        callback.onFailure(new TaException("Could not set status of contents"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getMyContentStatuses(OnResponseCallback<List<ContentStatus>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetMyContentStatusTask(context) {
                @Override
                protected void onSuccess(List<ContentStatus> statuses) throws Exception {
                    super.onSuccess(statuses);

                    if (statuses == null) {
                        getMyContentStatusesFromLocal(callback, new TaException("Could not fetch status of contents"));
                    } else {

                        for (ContentStatus status : statuses) {
                            status.setUsername(loginPrefs.getUsername());
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContentStatuses(statuses);
                            }
                        }.start();

                        callback.onSuccess(statuses);

                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getMyContentStatusesFromLocal(callback, ex);
                }
            }.execute();

        } else {
            getMyContentStatusesFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getMyContentStatusesFromLocal(OnResponseCallback<List<ContentStatus>> callback, Exception e) {

        new Task<List<ContentStatus>>(context) {
            @Override
            public List<ContentStatus> call() {
                return mLocalDataSource.getMyContentStatuses(loginPrefs.getUsername());
            }

            @Override
            protected void onSuccess(List<ContentStatus> contentStatuses) throws Exception {
                super.onSuccess(contentStatuses);

                if (contentStatuses != null) {
                    callback.onSuccess(contentStatuses);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getUserContentStatus(List<Long> contentIds, OnResponseCallback<List<ContentStatus>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetUserContentStatusTask(context, contentIds) {
                @Override
                protected void onSuccess(List<ContentStatus> statuses) throws Exception {
                    super.onSuccess(statuses);

                    if (statuses == null) {
                        getUserContentStatusFromLocal(contentIds, callback, new TaException("Could not fetch status of contents"));
                    } else {

                        List<ContentStatus> finalStatuses = new ArrayList<>();
                        for (ContentStatus status : statuses) {
                            if (status.getError() == null) {
                                status.setUsername(loginPrefs.getUsername());
                                finalStatuses.add(status);
                            }
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertContentStatuses(finalStatuses);
                            }
                        }.start();

                        callback.onSuccess(finalStatuses);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getUserContentStatusFromLocal(contentIds, callback, ex);
                }
            }.execute();

        } else {
            getUserContentStatusFromLocal(contentIds, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getUserContentStatusFromLocal(List<Long> contentIds, OnResponseCallback<List<ContentStatus>> callback,
                                               Exception e) {

        new Task<List<ContentStatus>>(context) {
            @Override
            public List<ContentStatus> call() {
                return mLocalDataSource.getContentStatusesByContentIds(contentIds, loginPrefs.getUsername());
            }

            @Override
            protected void onSuccess(List<ContentStatus> contentStatuses) throws Exception {
                super.onSuccess(contentStatuses);

                if (contentStatuses != null) {
                    callback.onSuccess(contentStatuses);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getUnitStatus(String courseId, OnResponseCallback<List<UnitStatus>> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetUnitStatusTask(context, courseId) {
                @Override
                protected void onSuccess(List<UnitStatus> statuses) throws Exception {
                    super.onSuccess(statuses);
                    if (statuses != null) {

                        new Thread() {
                            @Override
                            public void run() {
                                for (UnitStatus status : statuses) {
                                    status.setCourse_id(courseId);
                                    status.setUsername(loginPrefs.getUsername());
                                }
                                mLocalDataSource.insertUnitStatuses(statuses);
                            }
                        }.start();

                        callback.onSuccess(statuses);
                    } else {
                        getUnitStatusFromLocal(courseId, callback, new TaException("Unable to fetch unit statuses"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getUnitStatusFromLocal(courseId, callback, ex);
                }
            }.execute();

        } else {
            getUnitStatusFromLocal(courseId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getUnitStatusFromLocal(String courseId, OnResponseCallback<List<UnitStatus>> callback, Exception e) {

        new Task<List<UnitStatus>>(context) {
            @Override
            public List<UnitStatus> call() {
                return mLocalDataSource.getUnitStatusByCourse(loginPrefs.getUsername(), courseId);
            }

            @Override
            protected void onSuccess(List<UnitStatus> unitStatuses) throws Exception {
                super.onSuccess(unitStatuses);

                if (unitStatuses != null) {
                    callback.onSuccess(unitStatuses);
                } else {
                    callback.onFailure(e);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void startScorm(String courseId, String blockId, OnResponseCallback<ScormStartResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            new StartScormTask(context, courseId, blockId) {
                @Override
                protected void onSuccess(ScormStartResponse scormStartResponse) throws Exception {
                    super.onSuccess(scormStartResponse);
                    if (callback != null) {
                        callback.onSuccess(scormStartResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    if (callback != null) {
                        callback.onFailure(ex);
                    }
                }
            }.execute();

        } else {
            if (callback != null) {
                callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            }
        }

    }

    public void getOtherUserAccount(String username, OnResponseCallback<Account> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetAccountTask(context, username) {
                @Override
                protected void onSuccess(Account account) throws Exception {
                    super.onSuccess(account);
                    if (account != null) {

                        new Thread() {
                            @Override
                            public void run() {
                                mLocalDataSource.insertAccount(account);
                            }
                        }.start();

                        callback.onSuccess(account);
                    } else {
                        getLocalOtherUserAccount(username, callback, new TaException("Invalid account"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getLocalOtherUserAccount(username, callback, ex);
                }
            }.execute();

        } else {
            getLocalOtherUserAccount(username, callback,
                    new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getLocalOtherUserAccount(String username, OnResponseCallback<Account> callback, Exception e) {

        new Task<Account>(context) {
            @Override
            public Account call() {
                return mLocalDataSource.getAccount(username);
            }

            @Override
            protected void onSuccess(Account account) throws Exception {
                super.onSuccess(account);

                if (account == null) {
                    callback.onFailure(e);
                } else {
                    callback.onSuccess(account);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();

    }

    public void getFollowStatus(String username, OnResponseCallback<FollowStatus> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetFollowStatusTask(context, username) {
                @Override
                protected void onSuccess(FollowStatus followStatus) throws Exception {
                    super.onSuccess(followStatus);

                    if (followStatus == null) {
                        callback.onFailure(new TaException("Follow status could not be fetched"));
                    } else {
                        callback.onSuccess(followStatus);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getWpUser(long userId, OnResponseCallback<User> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.getUser(userId, new WordPressRestResponse<User>() {
                @Override
                public void onSuccess(User result) {
                    if (result == null || result.getUsername() == null) {
                        callback.onFailure(new TaException(context.getString(R.string.user_not_available)));
                    } else {
                        callback.onSuccess(result);
                    }
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void setCustomFieldAttributes(OnResponseCallback<FieldInfo> callback) {

        if (NetworkUtil.isConnected(context)) {

            new GetGenericUserFieldInfoTask(context) {
                @Override
                protected void onSuccess(FieldInfo fieldInfo) {
                    loginPrefs.setMxGenericFieldInfo(fieldInfo);
                    if (callback != null) {
                        callback.onSuccess(fieldInfo);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    if (callback != null) {
                        callback.onFailure(ex);
                    }
                }
            }.execute();

        } else {
            if (callback != null) {
                callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            }
        }

    }

    public void getCustomFieldAttributes(OnResponseCallback<FieldInfo> callback) {
        FieldInfo fieldInfo = loginPrefs.getMxGenericFieldInfo();
        if (fieldInfo != null) {
            callback.onSuccess(fieldInfo);
        } else {
            setCustomFieldAttributes(callback);
        }
    }

    public void setConnectCookies() {
        new MxCookiesAPI().execute();
    }

    public void checkSurvey(Activity activity, SurveyType surveyType) {
        new MxSurveyAPI(context, activity, surveyType).execute();
    }

    public void updateFirebaseToken(Activity activity) {
        FirebaseUtil firebaseUtil = new FirebaseUtil(activity);
        firebaseUtil.syncFirebaseToken();
    }

    public void scheduleDeleteFeeds() {
        Intent intent = new Intent(context, DeleteFeedsReceiver.class);
        new AlarmManagerUtil(context).scheduleRepeatingAlarm(intent, Constants.REQUEST_CODE_DELETE_FEEDS,
                DateUtil.getEndOfDay(new Date()).getTime(), Constants.INTERVAL_DELETE_FEEDS);
    }

    private void unscheduleDeleteFeeds() {
        Intent intent = new Intent(context, DeleteFeedsReceiver.class);
        new AlarmManagerUtil(context).cancelAlarm(intent, Constants.REQUEST_CODE_DELETE_FEEDS);
    }

    public void deleteAllFeeds() {
        new Thread() {
            @Override
            public void run() {
                mLocalDataSource.deleteFeeds(loginPrefs.getUsername());
            }
        }.start();
    }

    public void logSMSHash() {
        AppSignatureHelper helper = new AppSignatureHelper(context);
        helper.getAppSignatures();
    }

    public void likeConnectComment(long commentId, OnResponseCallback<StatusResponse> callback) {

        if (NetworkUtil.isConnected(context)) {

            wpClientRetrofit.likeComment(commentId, loginPrefs.getWPUserId(), new WordPressRestResponse<StatusResponse>() {
                @Override
                public void onSuccess(StatusResponse result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            if (callback != null) {
                callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            }
        }

    }

    public void getFollowersOrFollowing(boolean follower, int take, int skip, OnResponseCallback<List<SuggestedUser>> callback) {
        if (NetworkUtil.isConnected(context)) {

            new GetFollowersOrFollowingTask(context, follower, take, skip) {
                @Override
                protected void onSuccess(List<SuggestedUser> suggestedUsers) throws Exception {
                    super.onSuccess(suggestedUsers);
                    if (suggestedUsers == null) {
                        callback.onFailure(new TaException(
                                follower ? "No followers found" : "No following found"
                        ));
                    } else {
                        callback.onSuccess(suggestedUsers);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void setContentIdForLegacyDownloads() {

        if (NetworkUtil.isConnected(context)) {

            List<VideoModel> wpDownloads = edxEnvironment.getStorage().getLegacyWPDownloads();
//            List<VideoModel> edxDownloads = edxEnvironment.getStorage().getLegacyEdxDownloads();

            if (wpDownloads != null) {

                for (VideoModel model : wpDownloads) {
                    try {
                        getPostById(Long.parseLong(model.getVideoId()), new OnResponseCallback<Post>() {
                            @Override
                            public void onSuccess(Post data) {
                                getContentFromSourceIdentity(data.getSlug(), new OnResponseCallback<Content>() {
                                    @Override
                                    public void onSuccess(Content data) {
                                        model.setContent_id(data.getId());
                                        model.setChapterName(data.getSource().getName());
                                        edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(),
                                                model, null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*if (edxDownloads != null) {

                for (VideoModel model : edxDownloads) {
                    getContentFromSourceIdentity(model.getEnrollmentId(), new OnResponseCallback<Content>() {
                        @Override
                        public void onSuccess(Content data) {
                            model.setContent_id(data.getId());
                            edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(),
                                    model, null);
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                }
            }*/
        }
    }

    public void setContentIdForLegacyDownload(VideoModel model){
        if ((model.getDownloadType() != null && model.getDownloadType().equals(ContentType.Scrom.name())) ||
                (model.getFilePath() != null && model.getFilePath().equals(ContentType.Scrom.name()))) {

            /*getContentFromSourceIdentity(model.getEnrollmentId(), new OnResponseCallback<Content>() {
                @Override
                public void onSuccess(Content data) {
                    model.setContent_id(data.getId());
                    edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(), model, null);
                }

                @Override
                public void onFailure(Exception e) {
                    edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(), model, null);
                }
            });*/

        } else if (model.getDownloadType() != null && model.getDownloadType().equals(ContentType.CONNECTVIDEO.name())){

            try {
                getPostById(Long.parseLong(model.getVideoId()), new OnResponseCallback<Post>() {
                    @Override
                    public void onSuccess(Post data) {
                        getContentFromSourceIdentity(data.getSlug(), new OnResponseCallback<Content>() {
                            @Override
                            public void onSuccess(Content data) {
                                model.setContent_id(data.getId());
                                model.setChapterName(data.getSource().getName());
                                edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(),
                                        model, null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(), model, null);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(), model, null);
                    }
                });
            } catch (NumberFormatException e) {
                e.printStackTrace();
                edxEnvironment.getStorage().updateInfoByVideoId(model.getVideoId(), model, null);
            }

        }
    }

    public void scheduleSyncAnalyticsJob() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName componentName = new ComponentName(context, SyncAnalyticsJob.class);
            JobInfo.Builder builder = new JobInfo.Builder(12, componentName);
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                builder.setPeriodic(Constants.INTERVAL_SYNC_ANALYTICS_JOB);
            } else {
                builder.setMinimumLatency(Constants.INTERVAL_SYNC_ANALYTICS_JOB);
            }
            JobInfo jobInfo = builder.build();

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("_____TAG_____", "Sync analytics job scheduled!");
            } else {
                Log.d("_____TAG_____", "Sync analytics job not scheduled");
            }
        }
    }

    public void getUpdatedVersion(OnResponseCallback<UpdateResponse> callback,
                                  String version_name ,Long version_code ) {

        if (NetworkUtil.isConnected(context)) {
            new GetVersionUpdatedTask(context,version_name,version_code) {
                @Override
                protected void onSuccess(UpdateResponse versionResponse) throws Exception {
                    super.onSuccess(versionResponse);
                    callback.onSuccess(versionResponse);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        }
    }


    public boolean checkUpdate() {
        boolean isupdate = false;
        Date lastSeenDate,current_date;
        current_date = Calendar.getInstance().getTime();

        String lastUpdatedDate_str = getAppPref().getUpdateSeenDate();
        if (lastUpdatedDate_str==null || lastUpdatedDate_str.isEmpty())
            return true;

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
                lastSeenDate = format.parse(lastUpdatedDate_str);

            if (lastSeenDate == null)
                return true;

            long different = lastSeenDate.getTime() - current_date.getTime();
            long elapsedDays;
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            elapsedDays = different / daysInMilli;

            if (elapsedDays >= 1) {
                isupdate = true;
            } else {
                isupdate = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isupdate;
    }


    public Long getCurrent_vCode() {
        long v_code=0L;
        PackageManager pm = context.getPackageManager();
        PackageInfo info;

        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
            v_code= (long) info.versionCode;

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        return v_code;
    }

    public String getCurrentV_name() {
        String v_name= "";
        PackageManager pm = context.getPackageManager();
        PackageInfo info;

        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
            v_name=info.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        return v_name;
    }

    public void showToastFromOtherThread(String msg, int duration){
        mHandler.post(() -> Toast.makeText(context, msg, duration).show());
    }
}

