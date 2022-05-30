package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

public class MyProgramTags {
    private String tag_title;

    public String getTag_title() {
        return tag_title;
    }

    public void setTag_title(String tag_title) {
        this.tag_title = tag_title;
    }

    public String getConverted_tag_title() {
        return converted_tag_title;
    }

    public void setConverted_tag_title(String converted_tag_title) {
        this.converted_tag_title = converted_tag_title;
    }
    @SerializedName("converted_tag_title")
    private String converted_tag_title;
}
