package org.edx.mobile.discovery.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseCourseModel {

    @SerializedName("id")
    private String id;

    @SerializedName("data")
    private List<CourseItem> data;

    public String getId() {
        return id;
    }

    public List<CourseItem> getData() {
        return data;
    }

    public static class CourseItem {

        @SerializedName("converted_title")
        private String convertedTitle;

        @SerializedName("title")
        private String title;

        @SerializedName("key")
        private String key;

        public String getConvertedTitle() {
            return convertedTitle;
        }

        public String getTitle() {
            return title;
        }

        public String getKey() {
            return key;
        }
    }
}
