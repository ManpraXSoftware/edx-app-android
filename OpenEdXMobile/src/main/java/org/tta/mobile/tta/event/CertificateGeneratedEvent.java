package org.tta.mobile.tta.event;

public class CertificateGeneratedEvent {

    private String courseId;

    public CertificateGeneratedEvent(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
