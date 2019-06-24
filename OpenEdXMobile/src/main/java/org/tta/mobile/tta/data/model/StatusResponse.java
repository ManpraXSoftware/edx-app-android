package org.tta.mobile.tta.data.model;

public class StatusResponse {

    private boolean status;

    public StatusResponse() {
    }

    public StatusResponse(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
