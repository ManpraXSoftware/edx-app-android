package org.tta.mobile.tta.event;

import org.tta.mobile.model.VideoModel;

public class DownloadFailedEvent {

    private int errorCode;
    private VideoModel downloadEntry;

    public DownloadFailedEvent(int errorCode, VideoModel downloadEntry) {
        this.errorCode = errorCode;
        this.downloadEntry = downloadEntry;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public VideoModel getDownloadEntry() {
        return downloadEntry;
    }

    public void setDownloadEntry(VideoModel downloadEntry) {
        this.downloadEntry = downloadEntry;
    }
}
