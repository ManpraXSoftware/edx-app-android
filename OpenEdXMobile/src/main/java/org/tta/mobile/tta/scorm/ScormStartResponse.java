package org.tta.mobile.tta.scorm;

import com.google.gson.annotations.SerializedName;

public class ScormStartResponse {

    @SerializedName("status")
    private boolean success;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

}
