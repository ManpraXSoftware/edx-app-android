package org.edx.mobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatbotModal {

    @SerializedName("data")
    public String data;

    @SerializedName("sessionid")
    public String sessionid;

    public String getText() {
        return data;
    }
    
    public String getSessionid(){ return sessionid; }
}
