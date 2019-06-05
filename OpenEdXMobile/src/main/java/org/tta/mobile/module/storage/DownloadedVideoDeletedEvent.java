package org.tta.mobile.module.storage;

import org.tta.mobile.model.VideoModel;

public class DownloadedVideoDeletedEvent {

    private VideoModel model;

    public DownloadedVideoDeletedEvent(VideoModel model) {
        this.model = model;
    }

    public VideoModel getModel() {
        return model;
    }
}
