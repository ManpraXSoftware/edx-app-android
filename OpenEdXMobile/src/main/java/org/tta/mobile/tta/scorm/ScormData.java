package org.tta.mobile.tta.scorm;

import com.google.gson.annotations.SerializedName;

import org.tta.mobile.model.course.BlockData;

public class ScormData extends BlockData {

    @SerializedName("last_modified")
    public String lastModified;

    @SerializedName("scorm_data")
    public String scormData;

    @SerializedName("scorm_image_url")
    public String scormImageUrl;

    @SerializedName("articulate_type")
    public String articulateType;

    @SerializedName("scorm_duration")
    public String scormDuration;

}
