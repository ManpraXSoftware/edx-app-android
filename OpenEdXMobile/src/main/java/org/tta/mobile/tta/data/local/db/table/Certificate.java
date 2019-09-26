package org.tta.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import androidx.annotation.Nullable;

@Entity(tableName = "certificate", primaryKeys = {"course_id", "username"})
public class Certificate implements Parcelable {

    @NonNull
    private String username;

    private String status;

    private boolean regenerate;

    private String created;

    private String grade;

    @NonNull
    private String course_id;

    private String course_name;

    private String download_url;

    private String modified;

    private String image;

    public Certificate() {
    }

    protected Certificate(Parcel in) {
        username = in.readString();
        status = in.readString();
        regenerate = in.readByte() != 0;
        created = in.readString();
        grade = in.readString();
        course_id = in.readString();
        course_name = in.readString();
        download_url = in.readString();
        modified = in.readString();
        image = in.readString();
    }

    public static final Creator<Certificate> CREATOR = new Creator<Certificate>() {
        @Override
        public Certificate createFromParcel(Parcel in) {
            return new Certificate(in);
        }

        @Override
        public Certificate[] newArray(int size) {
            return new Certificate[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(status);
        dest.writeByte((byte) (regenerate ? 1 : 0));
        dest.writeString(created);
        dest.writeString(grade);
        dest.writeString(course_id);
        dest.writeString(course_name);
        dest.writeString(download_url);
        dest.writeString(modified);
        dest.writeString(image);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Certificate && TextUtils.equals(((Certificate) obj).course_id, course_id);
    }
}
