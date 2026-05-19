package org.edx.mobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatbotModal {

    public long messageId;

    @SerializedName("data")
    public String data;

    @SerializedName("sessionid")
    public String sessionid;

    @SerializedName("course_ids")
    public List<String> courseIds;

    @SerializedName("program_uuids")
    public List<String> programUuids;

    // Getters
    public String getData() {
        return data;
    }

    public String getSessionid() {
        return sessionid;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public List<String> getProgramUuids() {
        return programUuids;
    }

    public long getMessageId() {
        return messageId;
    }


    // Setters
    public void setData(String data) {
        this.data = data;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public void setProgramUuids(List<String> programUuids) {
        this.programUuids = programUuids;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }


}
