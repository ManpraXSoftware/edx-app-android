package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

public class ResumePrograms {
    private String block_id;

    public String getBlock_id() {
        return block_id;
    }

    public void setBlock_id(String block_id) {
        this.block_id = block_id;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    private String course_id;

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    private String course_name;

    public String getConverted_course_name() {
        return converted_course_name;
    }

    private String course_language;

    public void setConverted_course_name(String converted_course_name) {
        this.converted_course_name = converted_course_name;
    }
    @SerializedName("converted_course_name")
    private String converted_course_name;

    public String getCourse_language() {
        return course_language;
    }

    public void setCourse_language(String course_language) {
        this.course_language = course_language;
    }
}
