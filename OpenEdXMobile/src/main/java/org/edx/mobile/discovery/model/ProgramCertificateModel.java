package org.edx.mobile.discovery.model;

import com.google.gson.annotations.SerializedName;

public class ProgramCertificateModel {

    private boolean status;

    @SerializedName("certificate_url")
    private String certificateUrl;

    @SerializedName("program_name")
    private String programName;

    private String message;

    public boolean isStatus() {
        return status;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public String getProgramName() {
        return programName;
    }

    public String getMessage() {
        return message;
    }
}
