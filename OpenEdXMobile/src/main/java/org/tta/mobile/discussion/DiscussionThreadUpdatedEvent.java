package org.tta.mobile.discussion;

import androidx.annotation.NonNull;

public class DiscussionThreadUpdatedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadUpdatedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
