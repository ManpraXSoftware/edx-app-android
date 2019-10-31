package org.humana.mobile.tta.event;

import org.humana.mobile.tta.data.local.db.table.ContentStatus;

import java.util.List;

public class ContentStatusesReceivedEvent {

    private List<ContentStatus> statuses;

    public ContentStatusesReceivedEvent(List<ContentStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ContentStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ContentStatus> statuses) {
        this.statuses = statuses;
    }
}
