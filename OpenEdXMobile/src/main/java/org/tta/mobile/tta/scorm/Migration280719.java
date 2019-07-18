package org.tta.mobile.tta.scorm;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.tta.data.DataManager;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.moveDirectory;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;
import static org.tta.mobile.util.BrowserUtil.environment;

public class Migration280719 {

    private File oldFolderPath;
    private File newFolderPath;
    private File packDir;
    private File downloadsDir;

    private Context context;
    private DataManager dataManager;

    public Migration280719(Context context, DataManager dataManager) {
        File android = new File(Environment.getExternalStorageDirectory(), "Android");
        downloadsDir = new File(android, "data");
        packDir = new File(downloadsDir, context.getPackageName());
        oldFolderPath = new File(packDir, "scormFolder");
        newFolderPath = new File(packDir, "videos");

        this.context = context;
        this.dataManager = dataManager;

        if (!newFolderPath.exists())
            newFolderPath.mkdirs();
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

    public void MigrateScromPackages() {
        try {
            if (oldFolderPath.exists()) {
                //get all user specific dir i.e 9816733855 ,9599368558 etc.
                File[] user_directories = getSubDirectories(oldFolderPath);

                if (user_directories == null || user_directories.length <= 0)
                    return;

                for (File user_dir : user_directories) {

                    //go for scorm folders inside 9816733855/ec444554564333543gr435345334534
                    //user specific encrypted dir for edx structure
                    File userSHA1_dir = new File(newFolderPath, "" + getSHA1(user_dir.getName()));
                    userSHA1_dir.mkdirs();

                    //get all legacy entries from db
                    ArrayList<VideoModel> downloads = getLagacyScromEntries();

                    //get and update file one by one
                    for (VideoModel vm : downloads) {
                        //encrypt the video_id with SHA1 to find file name
                        File mFile = getSHA1Dir(vm.getVideoId(), user_dir);

                        if (!mFile.exists())
                            continue;

                        //now put that file inside edx file storing structure
                        // i.e  root/mobile.tta.org/video/SHA1(user)/scrom file.
                        File nFile = new File(userSHA1_dir, getSHA1(vm.getVideoId()));
                        if (!MoveDirectory(mFile, nFile))
                            return;

                        //update download entry path in db
                        updateLagacyDownload(vm, nFile.getAbsolutePath());

                        Log.e("MX_Scorm_Migration", "Scorm migration --------> fail ");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MX_Scorm_Migration", "Scorm migration fail --------> " + e.toString());
        }
    }

    public void deleteScormPackages(){
        try {
            deleteLegacyScromEntries();
            if (oldFolderPath.exists()) {
                FileUtils.deleteDirectory(oldFolderPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void MigrateConnectVideos() {

        try {
            //get all user specific dir i.e 9816733855 ,9599368558 etc.
            File[] user_directories = getSubDirectories(packDir);

            if (user_directories == null || user_directories.length <= 0)
                return;

            for (File user_dir : user_directories) {

                //go for connect folders inside 9816733855/ec444554564333543gr435345334534.mp4
                // ,9816733855/ec444554564333543gr435345334534.mp3
                if (user_dir.getName().trim().length() != 10 || user_dir.length() <= 0)
                    continue;

                //user spcific encripted dir for edx structure
                File userSHA1_dir = new File(newFolderPath, "" + getSHA1(user_dir.getName()));
                userSHA1_dir.mkdirs();

                //get all lagacy entries from db
                ArrayList<VideoModel> downloads = getLagacyConnectEntries();

                String lagacyFileName;
                //get and update file one by one
                for (VideoModel vm : downloads) {
                    //enrcipt the url with SHA1 to find file name
                    lagacyFileName=getLagacyFileName(vm.getFilePath());
                    File mFile = (new File(user_dir, lagacyFileName));

                    if (!mFile.exists())
                        continue;

                    //now put that file inside edx file storing structure
                    // i.e  root/mobile.tta.org/video/SHA1(user)/scrom file.
                    if (!MoveFile(mFile, userSHA1_dir))
                        continue;

                    //update download entry path in db
                    updateLagacyDownload(vm, userSHA1_dir.getAbsolutePath()+"/"+lagacyFileName);

                    Log.e("MX_Connect_Migration","Connect migration --------> Done ");
                }
            }
        }
        catch (Exception e)
        {
            Log.e("MX_Connect_Migration","Connect migration fail --------> "+e.toString());
        }
    }

    private void updateLagacyDownload(VideoModel vm, String file_path) {
        //set download url to null because are downloading file from scrom manager not from edx vedio download manager.
        vm.setFilePath(file_path);
        vm.setUsername(getSHA1(vm.getUsername()));

        if (NetworkUtil.isConnected(context)) {
            dataManager.setContentIdForLegacyDownload(vm);
        } else {
            environment.getStorage().updateInfoByVideoId(vm.getVideoId(), vm, null);
        }
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

    private void deleteLegacyScromEntries(){
        environment.getStorage().deleteLegacyScorms();
    }

    //it will provide you with folder containing scorm data which is associated with that particular scorm id
    private File getSHA1Dir(String url, File user_dir) {
        String hash = "";
        hash = Sha1Util.SHA1(url);
        File file = new File(user_dir, hash);
        return file;
    }

    private String getSHA1(String user_name) {
        return Sha1Util.SHA1(user_name);
    }

    private boolean MoveDirectory(File source, File dest) {
        try {
            moveDirectory(source, dest);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean MoveFile(File source, File dest) {
        try {
            moveFileToDirectory(source, dest,false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getLagacyFileName(String filepath) {
        if (filepath.isEmpty())
            return "";

        if (filepath.split("/") == null || filepath.split("/").length < 9)
            return "";

        return filepath.split("/")[filepath.split("/").length - 1];
    }
}
