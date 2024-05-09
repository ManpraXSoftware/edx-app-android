package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationReadModel {
        @SerializedName("message")
        private int message;
        public int getId() {
            return message;
        }

        public void setId(int message) {
            this.message = message;
        }
}