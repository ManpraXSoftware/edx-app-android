package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationModel {

    @SerializedName("unread_count")
    private int unreadCount;

    @SerializedName("data")
    private List<NotificationData> data;

    @SerializedName("next")
    private boolean next;

    @SerializedName("previous")
    private boolean previous;

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<NotificationData> getData() {
        return data;
    }

    public void setData(List<NotificationData> data) {
        this.data = data;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public boolean isPrevious() {
        return previous;
    }

    public void setPrevious(boolean previous) {
        this.previous = previous;
    }

    public static class NotificationData {

        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("notification_type")
        private String notificationType;

        @SerializedName("message")
        private String message;

        @SerializedName("is_read")
        private boolean isRead;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("course_id")
        private String courseId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getCourse() {
            return courseId;
        }

        public void setCourse(String courseId) {
            this.courseId = courseId;
        }

    }
}