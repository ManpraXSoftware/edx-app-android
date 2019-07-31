package org.tta.mobile.module.storage;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.tta.mobile.course.CourseAPI;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.model.api.VideoResponseModel;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.course.VideoBlockModel;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.model.download.NativeDownloadModel;
import org.tta.mobile.module.db.DataCallback;
import org.tta.mobile.module.db.DatabaseModelFactory;
import org.tta.mobile.module.db.IDatabase;
import org.tta.mobile.module.db.impl.DatabaseFactory;
import org.tta.mobile.module.download.IDownloadManager;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.module.prefs.UserPrefs;
import org.tta.mobile.module.prefs.VideoPrefs;
import org.tta.mobile.tta.analytics.AnalyticModel;
import org.tta.mobile.tta.data.enums.DownloadType;
import org.tta.mobile.tta.scorm.ScormBlockModel;
import org.tta.mobile.tta.tincan.model.Resume;
import org.tta.mobile.util.Config;
import org.tta.mobile.util.FileUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.Sha1Util;
import org.tta.mobile.view.BulkDownloadFragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.greenrobot.event.EventBus;

@Singleton
public class Storage implements IStorage {

    @Inject
    private Context context;
    @Inject
    private IDatabase db;
    @Inject
    private IDownloadManager dm;
    @Inject
    private UserPrefs pref;
    @Inject
    private Config config;
    @Inject
    private LoginPrefs loginPrefs;
    @Inject
    private CourseAPI api;
    @Inject
    private VideoPrefs videoPrefs;

    private final Logger logger = new Logger(getClass().getName());


    public long addDownload(VideoModel model) {
        if((model.getVideoUrl()==null||model.getVideoUrl().length()<=0) && model.getFilePath()!=null &&
                (!model.getFilePath().equalsIgnoreCase(String.valueOf(DownloadType.Scrom)) ||
                        !model.getFilePath().equalsIgnoreCase(String.valueOf(DownloadType.Pdf)) )){
            return -1;
        }
        else if(model!=null && model.getFilePath()!=null &&(model.getFilePath().equalsIgnoreCase(String.valueOf(DownloadType.Scrom)) ||
                model.getFilePath().equalsIgnoreCase(String.valueOf(DownloadType.Pdf))))
        {
            model.setDownloadedStateForScrom(DownloadEntry.DownloadedState.DOWNLOADED);

            db.addVideoData(model, null);
            return -1;
        }

        VideoModel videoByUrl = db.getVideoByVideoUrl(model.getVideoUrl(), null);

        db.addVideoData(model, null);

        if(model.isVideoForWebOnly())
            return -1;  //we may need to return different error code.
                        //but for now we showLoading same generic error message
        //IVideoModel videoById = db.getVideoEntryByVideoId(model.getVideoId(), null);

        if (videoByUrl == null || videoByUrl.getDmId() < 0) {
            boolean downloadPreference = pref.isDownloadOverWifiOnly();
            if(NetworkUtil.isOnZeroRatedNetwork(context, config)){
                //If the device has zero rated network, then allow downloading
                //on mobile network even if user has "Only on wifi" settings as ON
                downloadPreference = false;
            }

            // Fail the download if download directory isn't available
            final File downloadDirectory = pref.getDownloadDirectory();
            if (downloadDirectory == null) return -1;

            long dmid=-1;
            // there is no any download ever marked for this URL
            // so, add a download and map download info to given video
            if (model.getattachType()){
                dmid = dm.addMXDownload(downloadDirectory, model.getVideoUrl(),
                        model.getattachType(), model.getTitle());
            } else {
                dmid = dm.addDownload(downloadDirectory, model.getVideoUrl(),
                        downloadPreference, model.getTitle());
            }
            if(dmid==-1){
                //Download did not start for the video because of an issue in DownloadManager
                return -1;
            }
            NativeDownloadModel download = dm.getDownload(dmid);
            if(download!=null){
                // copy download info
                model.setDownloadingInfo(download);
            }
        } else {
            // download for this URL already exists, just map download info to given video
            model.setDownloadInfo(videoByUrl);
        }

        db.updateDownloadingVideoInfoByVideoId(model, new DataCallback<Integer>() {
            @Override
            public void onResult(Integer noOfRows) {
                if (noOfRows > 1) {
                    logger.warn("Should have updated only one video, " +
                            "but seems more than one videos are updated");
                }
                logger.debug("Video download info updated for " + noOfRows + " videos");
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });

        return model.getDmId();
    }

    @Override
    public int removeDownload(VideoModel model) {
        // FIXME: Refactor this function to use the list variant of removeDownload function below.
        int count = db.getVideoCountByVideoUrl(model.getVideoUrl(), null);
        if (count <= 1) {
            // if only one video exists, then mark it as DELETED
            // Also, remove its downloaded file
            dm.removeDownloads(model.getDmId());

            deleteFile(model.getFilePath());
        }

        // anyways, we mark the video as DELETED
        int videosDeleted = db.deleteVideoByVideoId(model, null);
        // Reset the state of Videos Bulk Download view whenever a delete happens
        videoPrefs.setBulkDownloadSwitchState(BulkDownloadFragment.SwitchState.DEFAULT, model.getEnrollmentId());
        EventBus.getDefault().post(new DownloadedVideoDeletedEvent(model));
        return videosDeleted;
    }

    @Override
    public int removeDownloads(List<VideoModel> modelList) {
        final int deletedVideos = removeDownloadsFromApp(modelList, null);
        logger.debug("Number of downloads removed by Download Manager: " + deletedVideos);
        EventBus.getDefault().post(new DownloadedVideoDeletedEvent(modelList.isEmpty()?null:modelList.get(0)));
        return deletedVideos;
    }

    @Override
    public void removeAllDownloads() {
        final String username = loginPrefs.getUsername();
        final String sha1Username;
        if (TextUtils.isEmpty(username)) {
            return;
        } else {
            sha1Username = Sha1Util.SHA1(username);
        }
        // Get all on going downloads
        db.getListOfOngoingDownloads(new DataCallback<List<VideoModel>>(false) {
            @Override
            public void onResult(List<VideoModel> result) {
                removeDownloadsFromApp(result, sha1Username);
                EventBus.getDefault().post(new DownloadedVideoDeletedEvent(result.isEmpty()?null:result.get(0)));
            }

            @Override
            public void onFail(Exception ex) {
            }
        });
    }

    private int removeDownloadsFromApp(List<VideoModel> result, String username) {
        if (result == null || result.size() <= 0) {
            return 0;
        }
        // Remove all downloads from NativeDownloadManager
        final long[] videoIds = new long[result.size()];
        for (int i = 0; i < result.size(); i++) {
            videoIds[i] = result.get(i).getDmId();
        }
        final int downloadsRemoved = dm.removeDownloads(videoIds);
        // Remove all downloads from db
        VideoModel model;
        for (int i = 0; i < result.size(); i++) {
            model = result.get(i);
            if (username == null) {
                db.deleteVideoByVideoId(model, null);
            } else {
                db.deleteVideoByVideoId(model, username, null);
            }
            deleteFile(model.getFilePath());
        }
        return downloadsRemoved;
    }

    /**
     * Deletes the physical file identified by given absolute file path.
     * Returns true if delete succeeds or if file does NOT exist, false otherwise.
     * DownloadManager actually deletes the physical file when remove method is called.
     * So, this method might not be required for removing downloads.
     * @param filepath The file to delete
     * @return true if delete succeeds or if file does NOT exist, false otherwise.
     */
    private boolean deleteFile(String filepath) {
        try {
            if(filepath != null) {
                if (filepath.endsWith(".zip")) {
                    filepath = filepath.substring(0, filepath.length()-4);
                }
                File file = new File(filepath);

                if (file.exists()) {

                    if (file.isDirectory()){
                        FileUtil.deleteRecursive(file);
                        logger.debug("Deleted: " + file.getPath());
                        return true;
                    } else if (file.delete()) {
                        logger.debug("Deleted: " + file.getPath());
                        return true;
                    } else {
                        logger.warn("Delete failed: " + file.getPath());
                    }
                } else {
                    logger.warn("Delete failed, file does NOT exist: " + file.getPath());
                    return true;
                }
            }
        } catch(Exception e) {
            logger.error(e);
        }

        return false;
    }

    @Override
    public int deleteAllUnenrolledVideos() {
        //      Integer count = db.deletedDeactivatedVideos();
        return 0;
    }

    @Override
    public void getAverageDownloadProgressInChapter(String enrollmentId, String chapter,
            final DataCallback<Integer> callback) {
        List<Long> dmidList = db.getDownloadingVideoDmIdsForChapter(enrollmentId, chapter, null);
        if (dmidList == null || dmidList.isEmpty()) {
            callback.onResult(0);
            return;
        }

        try {
            long[] dmidArray = new long[dmidList.size()];
            for (int i=0; i< dmidList.size(); i++) {
                dmidArray[i] = dmidList.get(i);
            }
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            callback.sendException(ex);
            logger.error(ex);
        }
    }


    @Override
    public void getAverageDownloadProgress(final DataCallback<Integer> callback) {
        IDatabase db = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE );
        db.getListOfOngoingDownloads(new DataCallback<List<VideoModel>>() {

            @Override
            public void onResult(List<VideoModel> result) {
                long[] dmids = new long[result.size()];
                for (int i=0; i< result.size(); i++) {
                    dmids[i] = result.get(i).getDmId();
                }

                int averageProgress = dm.getAverageProgressForDownloads(dmids);
                callback.onResult(averageProgress);
            }

            @Override
            public void onFail(Exception ex) {
                callback.onFail(ex);
            }
        });
    }

    @Override
    public void getDownloadProgressOfCourseVideos(@Nullable String courseId,
                                                  final DataCallback<NativeDownloadModel> callback) {
        final IDatabase db = DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE);
        db.getListOfOngoingDownloadsByCourseId(courseId, new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                final long[] dmids = new long[result.size()];
                for (int i = 0; i < result.size(); i++) {
                    dmids[i] = result.get(i).getDmId();
                }

                callback.onResult(dm.getProgressDetailsForDownloads(dmids));
            }

            @Override
            public void onFail(Exception ex) {
                callback.onFail(ex);
            }
        });
    }

    @Override
    public void getDownloadProgressOfVideos(@NonNull List<CourseComponent> videoComponents,
                                            final DataCallback<NativeDownloadModel> callback) {
        final IDatabase db = DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE);
        db.getVideosByVideoIds(videoComponents, DownloadEntry.DownloadedState.DOWNLOADING,
                new DataCallback<List<VideoModel>>() {
                    @Override
                    public void onResult(List<VideoModel> result) {
                        final long[] dmids = new long[result.size()];
                        for (int i = 0; i < result.size(); i++) {
                            dmids[i] = result.get(i).getDmId();
                        }

                        callback.onResult(dm.getProgressDetailsForDownloads(dmids));
                    }

                    @Override
                    public void onFail(Exception ex) {
                        callback.onFail(ex);
                    }
                });
    }

    @Override
    public void getAverageDownloadProgressInSection(String enrollmentId,
            String chapter, String section, DataCallback<Integer> callback) {
        long[] dmidArray = db.getDownloadingVideoDmIdsForSection(enrollmentId, chapter, section, null);
        if (dmidArray == null || dmidArray.length == 0) {
            callback.onResult(0);
            return;
        }

        try {
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public VideoModel getDownloadEntryfromVideoResponseModel(
            VideoResponseModel vrm) {
        VideoModel video = db.getVideoEntryByVideoId(vrm.getSummary().getId(), null);
        if (video != null) {
            // we have a db entry, so return it
            return video;
        }

        return DatabaseModelFactory.getModel(vrm);
    }

    @Override
    public VideoModel getDownloadEntryFromVideoModel(VideoBlockModel block){
        VideoModel video = db.getVideoEntryByVideoId(block.getId(), null);
        if (video != null) {
            return video;
        }

        return DatabaseModelFactory.getModel(block.getData(), block);
    }

    @Override
    public VideoModel getDownloadEntryFromScormModel(ScormBlockModel block) {
        VideoModel video = db.getVideoEntryByVideoId(block.getId(), null);
        if (video != null) {
            return video;
        }

        return DatabaseModelFactory.getModel(block.getData(), block);
    }

    @Override
    public NativeDownloadModel getNativeDownload(long dmId) {
        return dm.getDownload(dmId);
    }

    @Override
    public DownloadEntry reloadDownloadEntry(DownloadEntry video) {
        try{
            DownloadEntry de = (DownloadEntry) db.getVideoEntryByVideoId(video.videoId, null);
            if (de != null) {
                video.lastPlayedOffset = de.lastPlayedOffset;
                video.watched = de.watched;
                video.downloaded = de.downloaded;
            }
            return video;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    @Override
    public void getDownloadProgressByDmid(long dmId,
            DataCallback<Integer> callback) {
        if (dmId == 0) {
            callback.onResult(0);
            return;
        }
        try {
            long[] dmidArray = new long[1];
            dmidArray[0] = dmId;

            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public void markDownloadAsComplete(long dmId,
            DataCallback<VideoModel> callback) {
        try{
            NativeDownloadModel nm = dm.getDownload(dmId);
            if (nm != null && nm.status == DownloadManager.STATUS_SUCCESSFUL) {
                {
                    DownloadEntry e = (DownloadEntry) db.getDownloadEntryByDmId(dmId, null);
                    e.downloaded = DownloadEntry.DownloadedState.DOWNLOADED;
                    e.filepath = nm.filepath;
                    if(e.size<=0){
                        e.size = nm.size;
                    }
                    e.downloadedOn = System.currentTimeMillis();
                    // update file duration
                    if(e.duration==0){
                        try {
                            MediaMetadataRetriever r = new MediaMetadataRetriever();
                            FileInputStream in = new FileInputStream(new File(e.filepath));
                            r.setDataSource(in.getFD());
                            int duration = Integer
                                    .parseInt(r
                                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                            e.duration = duration/1000;
                            logger.debug("Duration updated to : " + duration);
                            in.close();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                    db.updateDownloadCompleteInfoByDmId(dmId, e, null);
                    callback.sendResult(e);
                    if (e.filepath.endsWith(".zip")) {
                        unpackZip(e.filepath);
                    }
                    EventBus.getDefault().post(new DownloadCompletedEvent(e));
                }

            } else {
                // download not yet successful
                logger.debug("Download not yet completed");
            }
        }catch(Exception e){
            callback.sendException(e);
            logger.error(e);
        }
    }

    private boolean unpackZip(String file)
    {
        InputStream is;
        ZipInputStream zis;

        File withExt = new File(file);
        try
        {

            String filename;
            String folder = withExt.getParent();
            String name = withExt.getName().substring(0, withExt.getName().length()-4);

            is = new FileInputStream(withExt.getAbsoluteFile());
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {

                filename = ze.getName();


                if (ze.isDirectory()) {
                    File fmd = new File(folder+"/" +name+"/"+ filename);

                    fmd.mkdirs();
                    continue;
                }


                File tmp = new File(folder+"/" +name+"/"+ filename);

                File foldertmp = tmp.getParentFile();
                if(!foldertmp.exists()){
                    foldertmp.mkdirs();
                }

                FileOutputStream fout = new FileOutputStream(folder+"/" +name+"/"+ filename);


                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();

            withExt.delete();
        }
        catch(IOException e)
        {
            withExt.delete();
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Checks progress of all the videos that are being downloaded.
     * If progress of any of the downloads is 100%, then marks the video as DOWNLOADED.
     * NOTE - precondition - used only for app upgrade
     */
    public void repairDownloadCompletionData() {

            // attempt to repair the data
            Thread maintenanceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProfileModel profile = loginPrefs.getCurrentUserProfile();
                        if (profile == null) {
                            // user no logged in
                            return;
                        }

                        List<Long> dmidList = db.getAllDownloadingVideosDmidList(null);
                        for (Long d : dmidList) {
                            // for each downloading video, check the percentage progress
                            boolean downloadComplete = dm.isDownloadComplete(d);
                            if (downloadComplete) {
                                // this means download is completed
                                // so the video status should be marked as DOWNLOADED, not DOWNLOADING
                                // update the video status
                                markDownloadAsComplete(d, new DataCallback<VideoModel>() {
                                    @Override
                                    public void onResult(VideoModel result) {
                                        logger.debug("Video download marked as completed, dmid=" + result.getDmId());
                                    }

                                    @Override
                                    public void onFail(Exception ex) {
                                        logger.error(ex);
                                    }
                                });
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }
            });
            maintenanceThread.start();
    }

    @Override
    public DownloadEntry getPostVideo(String postId) {
        VideoModel video = db.getPostVideo(postId);
        if (video != null) {
            // we have a db entry, so return it
            return (DownloadEntry)video;
        }

        return null;
    }

    @Override
    public DownloadEntry getPostVideo(String p_id, String video_url) {
        VideoModel video = db.getPostVideo(p_id,video_url, null);
        if (video != null) {
            // we have a db entry, so return it
            return (DownloadEntry)video;
        }

        return null;
    }

    @NonNull
    @Override
    public long addAnalytic(AnalyticModel model) {
        return db.addAnalyticData(model, null);
    }

    @NonNull
    @Override
    public int removeAnalytics(String[] ids, String INQueryParams) {
        return  db.deleteAnalyticByAnalyticId(ids, INQueryParams,null);
    }

    @Override
    public ArrayList<AnalyticModel> getMxAnalytics(int batch_count, int status) throws Exception {
        return  db.getAnalytics(batch_count,status, null);
    }

    @Override
    public ArrayList<AnalyticModel> getTincanAnalytics(int batch_count, int status) throws Exception {
        return  db.getTincanAnalytics(batch_count,status, null);
    }

    @Override
    public void markVideoPlaying(DownloadEntry videoModel, final DataCallback<Integer> watchedStateCallback) {
        try {
            final DownloadEntry v = videoModel;
            if (v != null) {
                if (v.watched == DownloadEntry.WatchedState.UNWATCHED) {
                    videoModel.watched = DownloadEntry.WatchedState.PARTIALLY_WATCHED;

                    // video entry might not exist in the database, add it
                    db.addVideoData(videoModel, new DataCallback<Long>() {
                        @Override
                        public void onResult(Long result) {
                            try {
                                // mark this as partially watches, as playing has started
                                db.updateVideoWatchedState(v.getVideoId(), DownloadEntry.WatchedState.PARTIALLY_WATCHED,
                                        watchedStateCallback);
                            } catch (Exception ex) {
                                logger.error(ex);
                            }
                        }

                        @Override
                        public void onFail(Exception ex) {
                            logger.error(ex);
                        }
                    });
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Nullable
    private String getUsername() {
        String ret = null;
        ProfileModel profile = pref.getProfile();
        if (profile != null) {
            ret = profile.username;
        }

        return ret;
    }

    @NonNull
    public Integer  getDownloadedScromCount () throws Exception
    {
        String username = getUsername();
        Integer allCoursesScromCount=0;

        /*if (username != null) {
            for (EnrolledCoursesResponse enrolledCoursesResponse : api.getUserEnrolledCourses(username, true, context)) {
                int scromCount = db.getDownloadedScromCountByCourse(
                        enrolledCoursesResponse.getCourse().getId(), null);

                if (scromCount > 0) {
                    allCoursesScromCount=allCoursesScromCount+scromCount;
                }
            }

            for (EnrolledCoursesResponse enrolledCoursesResponse : api.getUserEnrolledCourses(username, true, context)) {
                int PdfCount = db.getDownloadedPdfCountByCourse(
                        enrolledCoursesResponse.getCourse().getId(), null);

                if (PdfCount > 0) {
                    allCoursesScromCount=allCoursesScromCount+PdfCount;
                }
            }
        }*/
        return allCoursesScromCount;
    }

    @NonNull
    public Integer  getDownloadedPdfCount () throws Exception
    {
        String username = getUsername();
        Integer allCoursesScromCount=0;

        /*if (username != null) {
            for (EnrolledCoursesResponse enrolledCoursesResponse : api.getUserEnrolledCourses(username, true, context)) {
                int scromCount = db.getDownloadedScromCountByCourse(
                        enrolledCoursesResponse.getCourse().getId(), null);

                if (scromCount > 0) {
                    allCoursesScromCount=allCoursesScromCount+scromCount;
                }
            }
        }*/
        return allCoursesScromCount;
    }

    @Override
    public Long addResumePayload(Resume resume) {
        return db.addResumePayload(resume);
    }

    @Override
    public Integer deleteResumePayload(String course_id, String unit_id) {
        return db.deleteResumePayload(course_id,unit_id);
    }

    @Override
    public Integer updateResumePayload(Resume resume) {
        return db.updateResumePayload(resume);
    }

    @Override
    public Resume getResumeInfo(String course_id, String unit_id) {
        return  db.getResumeInfo(course_id,unit_id);
    }

    @Override
    public int getAnalyticsCount() {
        return db.getAnalyticsCount(null);
    }

    @Override
    public ArrayList<VideoModel> getDownloadedScorm() {
        return db.getDownloadedScorm();
    }

    @Override
    public ArrayList<VideoModel> getDownloadedConnect() {
        return db.getDownloadedConnect();
    }

    @Override
    public void deleteLegacyScorms() {
        db.deleteLegacyScorms();
    }

    @Override
    public Integer updateInfoByVideoId(String videoId, VideoModel model,DataCallback<Integer> callback) {
        return db.updateInfoByVideoId(videoId,model,callback);
    }

    @Override
    public List<VideoModel> getLegacyWPDownloads() {
        return db.getLegacyWPDownloads();
    }

    @Override
    public List<VideoModel> getLegacyEdxDownloads() {
        return db.getLegacyEdxDownloads();
    }
}
