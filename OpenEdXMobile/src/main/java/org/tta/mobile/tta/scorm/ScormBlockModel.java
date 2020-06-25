package org.tta.mobile.tta.scorm;

import android.support.annotation.Nullable;

import org.tta.mobile.model.course.BlockModel;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.course.HasDownloadEntry;
import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.module.storage.IStorage;

public class ScormBlockModel extends CourseComponent implements HasDownloadEntry {

    private ScormData data;
    private DownloadEntry downloadEntry;

    public ScormBlockModel(BlockModel blockModel, CourseComponent parent) {
        super(blockModel, parent);
        this.data = (ScormData) blockModel.data;
    }

    public ScormData getData() {
        return data;
    }

    public void setData(ScormData data) {
        this.data = data;
    }

    @Nullable
    @Override
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                    .getDownloadEntryFromScormModel(this);
        }
        return downloadEntry;
    }

    @Nullable
    @Override
    public String getDownloadUrl() {
//        return "https://s3.amazonaws.com/scorm-file/Language/01/scorm/1c978694b04d4132b7e2fcec919ba014/d71840fe59904bb280c567cac608374cabdc67d0.zip";
        if (data == null){
            return null;
        }
        return data.scormData;
    }

    public String getLastModified(){
        if (data == null){
            return null;
        }
        return data.lastModified;
    }

    public String getArticulateType(){
        if (data == null || data.articulateType==null|| data.articulateType.isEmpty()){
            return null;
        }
        return data.articulateType;
    }

}
