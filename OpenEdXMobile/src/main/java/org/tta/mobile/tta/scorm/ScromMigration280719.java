package org.tta.mobile.tta.scorm;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.tta.mobile.course.CourseDetail;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.moveDirectory;
import static org.tta.mobile.util.BrowserUtil.environment;
import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class ScromMigration280719 {

    private Context context;
    private File oldFolderPath;
    private File scromFilePath;
    private File newFolderPath;


    public ScromMigration280719(Context context) {
        try {
            this.context = context;
            File android = new File(Environment.getExternalStorageDirectory(), "Android");
            File downloadsDir = new File(android, "data");
            File packDir = new File(downloadsDir, context.getPackageName());
            oldFolderPath = new File(packDir, "scormFolder");

            if (oldFolderPath.exists()) {
                //
                newFolderPath = new File(packDir, "videos");

                if (!newFolderPath.exists())
                    newFolderPath.mkdirs();

                //get all user specific dir i.e 9816733855 ,9599368558 etc.
                File[] user_directories = getSubDirectories(oldFolderPath);

                if (user_directories == null || user_directories.length <= 0)
                    return;

                for (File user_dir : user_directories) {

                    //go for scrom folders inside 9816733855/ec444554564333543gr435345334534

                    //user spcific encripted dir for edx structure
                    File userSHA1_dir = new File(newFolderPath, "" + getSHA1(user_dir.getName()));
                    userSHA1_dir.mkdirs();


                    //get all lagacy entries from db
                    ArrayList<VideoModel> downloads = getLagacyScromEntries();

                    //get and update file one by one
                    for (VideoModel vm : downloads) {
                        //enrcipt the video_id with SHA1 to find file name
                        File mFile = getSHA1Dir(vm.getVideoId(), user_dir);

                        if (!mFile.exists())
                            return;


                        //now put that file inside edx file storing structure
                        // i.e  root/mobile.tta.org/video/SHA1(user)/scrom file.

                        File nFile = new File(userSHA1_dir, getSHA1(vm.getVideoId()));
                        if (!copyFile(mFile, nFile))
                            return;

                        //update download entry path in db
                        updateLagacyDownload(vm, nFile.getAbsolutePath());

                    }
                }

            }

        } catch (Exception e) {
            Log.e("MX_Migration", "crash in constructor.");
        }
    }

    private File[] getSubDirectories(File base_dir) {
        File[] directories = base_dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        return directories;
    }


    private void updateLagacyDownload(VideoModel vm, String file_path) {
        //set download url to null because are downloading file from scrom manager not from edx vedio download manager.
        vm.setFilePath(file_path);
        environment.getStorage().updateInfoByVideoId(vm.getVideoId(),vm,null);
    }

    private ArrayList<VideoModel> getLagacyScromEntries() {
        ArrayList<VideoModel> arrayList = environment.getStorage().getDownloadedScorm();

        if (arrayList == null || arrayList.size() <= 0)
            return new ArrayList<>();
        else
            return arrayList;
    }

    private ArrayList<VideoModel> getLagacyConnectEntries() {
        ArrayList<VideoModel> arrayList = environment.getStorage().getDownloadedConnect();

        if (arrayList == null || arrayList.size() <= 0)
            return new ArrayList<>();
        else
            return arrayList;
    }

    //it will provide you with folder containing scorm data which is associated with that particular scorm id
    private File getSHA1Dir(String url, File user_dir) {
        String hash = "";
        hash = Sha1Util.SHA1(url);
        File file = new File(user_dir, hash);
        return file;
    }

    private String getSHA1(String user_name) {
        String hash = "";
        return Sha1Util.SHA1(user_name);
    }


    private boolean copyFile(File source,File dest)
    {
        try {
            moveDirectory(source,dest);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public void MigrateConnectVideos()
    {
        File android = new File(Environment.getExternalStorageDirectory(), "Android");
        File downloadsDir = new File(android, "data");
        File packDir = new File(downloadsDir, context.getPackageName());

        //get all directories with length 10,because we are storing it in username folder

        //get all user specific dir i.e 9816733855 ,9599368558 etc.
        File[] user_directories = getSubDirectories(packDir);

        if (user_directories == null || user_directories.length <= 0)
            return;

        for (File user_dir : user_directories) {

            //go for connect folders inside 9816733855/ec444554564333543gr435345334534.mp4
            // ,9816733855/ec444554564333543gr435345334534.mp3

            if(user_dir.getName().trim().length()!=10 || user_dir.length() <= 0)
                return;

            //user spcific encripted dir for edx structure
            File userSHA1_dir = new File(newFolderPath, "" + getSHA1(user_dir.getName()));
            userSHA1_dir.mkdirs();

            //get all lagacy entries from db
            ArrayList<VideoModel> downloads = getLagacyConnectEntries();

            //get and update file one by one
            for (VideoModel vm : downloads) {
                //enrcipt the video_id with SHA1 to find file name
                File mFile = getSHA1Dir(vm.getHLSVideoUrl(), user_dir);

                if (!mFile.exists())
                    return;


                //now put that file inside edx file storing structure
                // i.e  root/mobile.tta.org/video/SHA1(user)/scrom file.

                File nFile = new File(userSHA1_dir, getSHA1(vm.getHLSVideoUrl()));
                if (!copyFile(mFile, nFile))
                    return;

                //update download entry path in db
                updateLagacyDownload(vm, nFile.getAbsolutePath());

            }
        }




    }
}
