package org.tta.mobile.tta.scorm;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.scorm.ContentType;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.moveDirectory;
import static org.tta.mobile.util.BrowserUtil.environment;
import static org.tta.mobile.util.BrowserUtil.loginPrefs;
import static org.tta.mobile.util.BrowserUtil.scormManager;

/**
 * Created by Arjun Chauhan on 4/21/2017.
 */

public class ScormMigrationScript {
    private File oldScormFolderPath;
    private File newuserSpecificFolderPath;
    private String hash = "";
    private Context context;
    private boolean isOldFolderExist = false;
    private final Logger logger = new Logger(getClass().getName());
    // Buffer size used.
    private final static int BUFFER_SIZE = 1024;
    private String asset_folder = "tincan";
    private String master_file ="app.min.js";

    public ScormMigrationScript(Context context) {
        try {
            this.context = context;
            File android = new File(Environment.getExternalStorageDirectory(), "Android");
            File downloadsDir = new File(android, "data");
            File packDir = new File(downloadsDir, context.getPackageName());
            File scormFolder = new File(packDir, "scormFolder");

            if (scormFolder.exists()) {
                oldScormFolderPath = scormFolder;
                isOldFolderExist = true;

                //add user specific folder
                newuserSpecificFolderPath = new File(scormFolder, loginPrefs.getUsername());

                if (!newuserSpecificFolderPath.exists()) {
                    newuserSpecificFolderPath.mkdirs();
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void doMigrate(ScormBlockModel download) {
        if (has(download.getId())) {
            boolean ismoved = false;
            //get encripted folder path first
            File Sha1EncriptedFolderPath = getSHAoneEncriptedFolderPath(download.getId());
            try {
                ismoved = true;
                //Create same folder inside userspecific folder with SHA1 code
                File hashFolder = new File(newuserSpecificFolderPath, hash);

                //move data to new folder
                moveDirectory(Sha1EncriptedFolderPath, hashFolder);
            } catch (IOException e) {
                e.printStackTrace();
                ismoved = false;
            }

            //if file moved successfully do entry in DB too..
            if (ismoved) {
                doDownloadEntery_db(download);
            }
        }
    }

    public boolean has(String url) {

        hash = Sha1Util.SHA1(url);
        File file = new File(oldScormFolderPath, hash);
        return file.exists() && file.isDirectory();
    }

    public boolean isOldFolderexist() {
        return isOldFolderExist;
    }

    //it will provide you with folder containing scorm data which is associated with that particular scorm id
    private File getSHAoneEncriptedFolderPath(String url) {
        String hash = "";
        hash = Sha1Util.SHA1(url);
        File file = new File(oldScormFolderPath, hash);
        return file;
    }

    private void doDownloadEntery_db(CourseComponent mUnit) {
        //set download url to null because are downloading file from scrom manager not from edx vedio download manager.
        DownloadEntry model = new DownloadEntry();
        model.setDownloadEntryForScrom(loginPrefs.getUsername(), mUnit.getDisplayName(), String.valueOf(ContentType.Scrom), mUnit.getId(), "", mUnit.getRoot().getCourseId()
                , mUnit.getParent().getDisplayName()
                , mUnit.getParent().getParent().getDisplayName(), (long) 0, String.valueOf(ContentType.Scrom));

        environment.getStorage().addDownload(model);
    }

    public void doAllAppMinMigration() {
        if(isMasterFileExist()) {
            //if yes then find package file location to replace
            ArrayList<VideoModel> arrayList = environment.getStorage().getDownloadedScorm();
            for (VideoModel item : arrayList) {
                migrateAppMin(item.getVideoId());
            }
        }
    }

    public void migrateAppMin(String unit_id)
    {
        Log.d("MX_Migration","Inside master file migration");
        //find if master file exist
        if(isMasterFileExist()) {
            if (scormManager.has(unit_id)) {
                File root_file;
                root_file = findFile(new File(scormManager.get(unit_id)), "", "tincan.xml");
                if(root_file == null || !root_file.exists())
                    return;

                String package_name = root_file.toString().split("/")[root_file.toString().split("/").length - 2];

                Log.d("MX_Migration", "find package for migration==> " + unit_id + " ::: " + package_name);

                String targeted_folder_path = scormManager.get(unit_id) + "/" + package_name + "/html5/lib/scripts";

                //Find old file to delete
                File file_to_delete = null;
                if (targeted_folder_path != null)
                    file_to_delete = findFile(targeted_folder_path, master_file);

                if (file_to_delete != null) {
                    file_to_delete.delete();
                    Log.d("MX_Migration", "deleted file==>" + master_file + "<== form package");
                }

                //if delete done successfully do copy new file to targted folder
                copyAssetFilePackage(asset_folder, master_file, targeted_folder_path);
            }
        }
    }

    private File findFile(File aFile, String sDir, String toFind) {
        if (aFile.isFile() &&
                aFile.getAbsolutePath().contains(sDir) &&
                aFile.getName().contains(toFind)) {
            return aFile;
        } else if (aFile.isDirectory()) {
            for (File child : aFile.listFiles()) {
                File found = findFile(child, sDir, toFind);
                if (found != null) {
                    return found;
                }//if
            }//for
        }//else
        return null;
    }

    private void copyAssetFilePackage(String folder_name, String file_name, String targeted_path) {
        try {

            AssetManager assetFiles = context.getAssets();
            // folder_name is the name of folder from inside our assets folder
            String[] files = assetFiles.list(folder_name);

            // Initialize streams
            InputStream in = null;
            OutputStream out = null;

            for (int i = 0; i < files.length; i++) {

                if (!files[i].toString().equalsIgnoreCase(file_name)) {
                    //  @This is to prevent the app from throwing file not found
                    // exception.
                } else {
                    // @Folder name is also case sensitive
                    // @folder_name is the folder from our assets

                    Log.d("MX_Migration","Start copying master file to package==>"+targeted_path);
                    in = assetFiles.open(folder_name + "/" + files[i]);
                    // Currently we will copy the files to the root directory
                    // but you should create specific directory for your app

                    /*out = new FileOutputStream(
                            Environment.getExternalStorageDirectory() + "/"
                                    + files[i]);*/

                    out = new FileOutputStream(
                            targeted_path + "/"
                                    + files[i]);

                    copyAssetFiles(in, out);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyAssetFiles(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            Log.d("MX_Migration","Migration successed");
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMasterFileExist() {
        boolean isMaster = false;

        try {
            AssetManager assetFiles = context.getAssets();
            // folder_name is the name of folder from inside our assets folder
            String[] files = assetFiles.list(asset_folder);

            // Initialize streams
            InputStream in = null;
            OutputStream out = null;

            for (int i = 0; i < files.length; i++) {

                if (!files[i].toString().equalsIgnoreCase(master_file)) {
                    //  @This is to prevent the app from throwing file not found
                    // exception.
                } else {
                    Log.d("MX_Migration","find master file=>"+files[i].toString());
                    isMaster = true;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isMaster;
    }

    private File findFile(String folderPath, String file_name) {
        File file = null;

        File dir = new File(folderPath);
        File[] files = dir.listFiles();
        if (files.length == 0) {
            System.out.println("The directory is empty");
        } else {
            for (File aFile : files) {
                if (aFile.getName().equals(file_name))
                    file = aFile;
            }
        }
        return file;
    }
}



