package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationReadModel {
        @SerializedName("message")
        private String message;
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
}