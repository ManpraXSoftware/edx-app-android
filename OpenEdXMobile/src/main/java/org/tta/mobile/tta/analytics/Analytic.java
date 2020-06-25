package org.tta.mobile.tta.analytics;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.http.HttpResponseStatusException;
import org.tta.mobile.http.HttpStatus;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.FormFieldMessageBody;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Page;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.model.SuccessResponse;
import org.tta.mobile.tta.task.content.course.scorm.GetAllDownloadedScromCountTask;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.Sha1Util;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.util.RoboContext;

import static org.tta.mobile.util.BrowserUtil.environment;
import static org.tta.mobile.util.BrowserUtil.loginPrefs;

/**
 * Created by manprax on 5/1/17.
 */

public class Analytic {

    private static final int ANALYTICS_COUNT_FOR_SYNC = 50;

    private Context ctx;
    private int analyticBatchCount = 100;

    @Inject
    private MxAnalyticsAPI mxAnalyticsAPI;

    @Inject
    public Analytic(Context mctx) {
        this.ctx = mctx;
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source, String actionId) {
        String username = loginPrefs.getUsername();

        if(!shouldISave(String.valueOf(action))) {
            Log.e("Analytcs", "Not saving-->" + String.valueOf(action) + " Its a depricated action");
            return;
        }

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = BreadcrumbUtil.getBreadcrumb();
            model.action_id = actionId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);

            if (getAnalyticsCount() >= ANALYTICS_COUNT_FOR_SYNC){
                syncAnalytics();
            }
        }
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source,
                                  String actionId, String sourceIdentity, long contentId) {

        if(!shouldISave(String.valueOf(action))) {
            Log.e("Analytcs", "Not saving-->" + String.valueOf(action) + " Its a depricated action");
            return;
        }

        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = BreadcrumbUtil.getBreadcrumb();
            model.action_id = actionId;
            model.source_id = sourceIdentity;
            model.content_id = contentId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);

            if (getAnalyticsCount() >= ANALYTICS_COUNT_FOR_SYNC){
                syncAnalytics();
            }
        }
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source, String actionId, String nav) {

        if(!shouldISave(String.valueOf(action))) {
            Log.e("Analytcs", "Not saving-->" + String.valueOf(action) + " Its a depricated action");
            return;
        }

        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = nav;
            model.action_id = actionId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);

            if (getAnalyticsCount() >= ANALYTICS_COUNT_FOR_SYNC){
                syncAnalytics();
            }
        }
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source,
                                  String actionId, String nav, String sourceIdentity, long contentId) {

        if(!shouldISave(String.valueOf(action))) {
            Log.e("Analytcs", "Not saving-->" + String.valueOf(action) + " Its a depricated action");
            return;
        }

        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = nav;
            model.action_id = actionId;
            model.source_id = sourceIdentity;
            model.content_id = contentId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);

            if (getAnalyticsCount() >= ANALYTICS_COUNT_FOR_SYNC){
                syncAnalytics();
            }
        }
    }

    public void addTinCanAnalyticDB(String tincan_obj, String course_name, String course_id,
                                    String sourceIdentity, long contentId) {
        String username = loginPrefs.getUsername();

        //save Tincan analytics to  local db first
        //download entry to db
        if (username == null || tincan_obj.isEmpty()) {
            Log.d("TinCanDbEntry", "unable to add");
            return;
        }

        AnalyticModel model = new AnalyticModel();
        model.user_id = username;
        model.action = String.valueOf(Action.TinCanObject);
        model.metadata = tincan_obj;
        //we are adding courseid to page in case of tincan
        model.page = course_name;
        model.source = String.valueOf(Source.Mobile);
        model.nav = BreadcrumbUtil.getBreadcrumb();
        model.action_id = course_id;
        model.source_id = sourceIdentity;
        model.content_id = contentId;
        model.setEvent_date();
        model.setStatus(0);

        environment.getStorage().addAnalytic(model);

        if (getAnalyticsCount() >= ANALYTICS_COUNT_FOR_SYNC){
            syncAnalytics();
        }
    }

    public void deleteAnalytics(ArrayList<AnalyticModel> analyticModelList) {
        environment.getStorage().removeAnalytics(getIds(analyticModelList), getINQueryParams(getIds(analyticModelList)));
    }

    //region:: utility functions
    private String getINQueryParams(String[] ids) {
        StringBuilder sb = new StringBuilder();

        if (ids == null || ids.length == 0)
            return "";

        for (int i = 0; i < ids.length; i++) {

            if (i == ids.length - 1)
                sb.append("?");
            else
                sb.append("?,");
        }

        return sb.toString();
    }

    public void syncAnalytics() {
        if (NetworkUtil.isConnected(ctx) && loginPrefs.isLoggedIn()) {
            syncMXAnalytics();
            syncTinCanAnalytics();
        }
    }

    private String[] getIds(ArrayList<AnalyticModel> analyticModelList) {
        String[] mIds = new String[analyticModelList.size() + 4];

        for (int index = 0; index < analyticModelList.size(); index++) {
            mIds[index] = analyticModelList.get(index).analytic_id;
        }
        return mIds;
    }

    private ArrayList<AnalyticModel> getMxAnalytics() {
        ArrayList<AnalyticModel> list = new ArrayList<>();
        try {
            list = environment.getStorage().getMxAnalytics(analyticBatchCount, 0);
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Analytic.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getMxAnalytics");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
            Log.d("Analtics", "Dbfetch fail");
        }
        return list;
    }

    private ArrayList<AnalyticModel> getTincanAnalytics() {
        ArrayList<AnalyticModel> list = new ArrayList<>();
        try {
            list = environment.getStorage().getTincanAnalytics(analyticBatchCount, 0);
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Analytic.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getTincanAnalytics");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
            Log.d("TincanAnaltics", "Dbfetch fail");
        }
        return list;
    }

    private int getAnalyticsCount(){
        try{
            return environment.getStorage().getAnalyticsCount();
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Analytic.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getAnalyticsCount");
            Logger.logCrashlytics(e, parameters);
            return 0;
        }
    }

    private void syncMXAnalytics() {
        ArrayList<AnalyticModel> list = getMxAnalytics();

        if (list == null || list.size() == 0)
            return;

        final ArrayList<AnalyticModel> finalList = list;
        final MXAnalyticsTask task = new MXAnalyticsTask(ctx, finalList) {
            @Override
            protected void onSuccess(SuccessResponse mx_AnalyticsResponse) throws Exception {
                super.onSuccess(mx_AnalyticsResponse);
                deleteAnalytics(finalList);
                Log.d("MXAnaltics", "successfully updated mx Normal analytics");
            }

            @Override
            protected void onException(Exception ex) {
                Log.d("MXAnaltics", "fail to update mx analytics");
            }
        };
        task.execute();
    }

    public void syncSingleMXAnalytic(String metadata, Action action, String page, Source source,
                                  String actionId, String nav, String sourceIdentity, long contentId) {
        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = nav;
            model.action_id = actionId;
            model.source_id = sourceIdentity;
            model.content_id = contentId;

            model.setEvent_date();
            model.setStatus(0);

            ArrayList<AnalyticModel> list = new ArrayList<>();
            list.add(model);
            final MXAnalyticsTask task = new MXAnalyticsTask(ctx, list) {
                @Override
                protected void onSuccess(SuccessResponse mx_AnalyticsResponse) throws Exception {
                    super.onSuccess(mx_AnalyticsResponse);
                    Log.d("MXAnaltics single", "successfully updated single mx Normal analytic");
                }

                @Override
                protected void onException(Exception ex) {
                    Log.d("MXAnaltics single", "fail to update single mx analytic");
                }
            };
            task.execute();
        }
    }

    private void syncTinCanAnalytics() {
        ArrayList<AnalyticModel> list = getTincanAnalytics();

        if (list == null || list.size() == 0)
            return;
        TincanRequest tincan_item = new TincanRequest();
        tincan_item.user_id = list.get(0).user_id;
        tincan_item.version = list.get(0).version;

        Tincan tincan = new Tincan();
        for (AnalyticModel item : list) {
            tincan = new Tincan();

            tincan.analytic_id = item.analytic_id;
            tincan.course_id = item.page;
            tincan.metadata = item.metadata;
            tincan.action_id = item.action_id;
            tincan.nav = item.nav;
            tincan.source_id = item.source_id;
            tincan.content_id = item.content_id;

            tincan_item.tincanObj.add(tincan);
        }

        final ArrayList<AnalyticModel> finalList = list;
        final TinCanAnalyticTask task = new TinCanAnalyticTask(ctx, tincan_item) {
            @Override
            protected void onSuccess(SuccessResponse mx_AnalyticsResponse) throws Exception {
                super.onSuccess(mx_AnalyticsResponse);
                deleteAnalytics(finalList);
                Log.d("MXAnaltics", "successfully updated mx TinCanAnalytics");
            }

            @Override
            protected void onException(Exception ex) {
                Log.d("MXAnaltics", "fail to update tincan analytics");
            }
        };
        task.execute();
    }

    private boolean shouldISave(String actionType) {
        /*Stop below analytics to save in db.
        Nav
                DeleteSection
        CertificateDownload
                ViewCert
        DBView
                DBLike
        DBComment
                DBCommentlike
        DBCommentReply
                BookmarkPost
        UnbookmarkPost
                DeletePost
        ChangeFilter
                Search
        ViewProfile
                ViewPoints
        ViewBadges
                PostFeedback
        OfflineSections
                Share*/
        boolean save = false;
        if (actionType.trim().toLowerCase().equals("Nav".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DeleteSection".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("CertificateDownload".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("ViewCert".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DBView".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("Share".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("OfflineSections".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("PostFeedback".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("ViewBadges".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("ViewPoints".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("ViewProfile".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("Search".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("ChangeFilter".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DeletePost".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("UnbookmarkPost".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("BookmarkPost".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DBCommentReply".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DBCommentlike".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DBComment".trim().toLowerCase()) ||
                actionType.trim().toLowerCase().equals("DBLike".trim().toLowerCase())) {
            save = false;
        } else {
            save = true;
        }
        return save;
    }
    //endregion
}
