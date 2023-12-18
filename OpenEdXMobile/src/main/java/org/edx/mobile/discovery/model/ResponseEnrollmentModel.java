package org.edx.mobile.discovery.model;

import com.google.gson.annotations.SerializedName;


public class ResponseEnrollmentModel {
        @SerializedName("status")
        private boolean status;

        @SerializedName("is_enrolled")
        private String enrollmentStatus;

        @SerializedName("message")
        private String message;

        // Constructors, getters, and setters

        public boolean isStatus() {
            return status;
        }

        public String getEnrollmentStatus() {
            return enrollmentStatus;
        }

        public String getMessage() {
            return message;
        }


}
