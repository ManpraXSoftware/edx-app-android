package org.tta.mobile.tta.event;

import org.tta.mobile.discussion.DiscussionThread;

public class DiscussionThreadUpdateEvent {

    private DiscussionThread thread;

    public DiscussionThreadUpdateEvent(DiscussionThread thread) {
        this.thread = thread;
    }

    public DiscussionThread getThread() {
        return thread;
    }

    public void setThread(DiscussionThread thread) {
        this.thread = thread;
    }
}
