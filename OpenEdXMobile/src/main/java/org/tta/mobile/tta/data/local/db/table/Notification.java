package org.tta.mobile.tta.data.local.db.table;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notification")
public class Notification implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int local_id;
    private long id;
    private String username;
    private String type;
    private String ref_id;
    private String title;
    private String description;
    private boolean seen;
    private long created_time;
    private boolean updated;

    public Notification() {
    }

    protected Notification(Parcel in) {
        local_id = in.readInt();
        id = in.readLong();
        username = in.readString();
        type = in.readString();
        ref_id = in.readString();
        title = in.readString();
        description = in.readString();
        seen = in.readByte() != 0;
        created_time = in.readLong();
        updated = in.readByte() != 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef_id() {
        return ref_id;
    }

    public void setRef_id(String ref_id) {
        this.ref_id = ref_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Notification && (((Notification) obj).local_id==local_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(local_id);
        dest.writeLong(id);
        dest.writeString(username);
        dest.writeString(type);
        dest.writeString(ref_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeByte((byte) (seen ? 1 : 0));
        dest.writeLong(created_time);
        dest.writeByte((byte) (updated ? 1 : 0));
    }

    public void set(Notification notification){
        if (local_id == 0) {
            local_id = notification.local_id;
        }
        if (id == 0) {
            id = notification.id;
        }
        username = notification.username;
        type = notification.type;
        ref_id = notification.ref_id;
        title = notification.title;
        description = notification.description;
        if (!seen) {
            seen = notification.seen;
        }
        created_time = notification.created_time;
        if (!updated) {
            updated = notification.updated;
        }
    }

    @Override
    public String toString() {
        return "Notification{" +
                "local_id=" + local_id +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", ref_id='" + ref_id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", seen=" + seen +
                ", created_time=" + created_time +
                ", updated=" + updated +
                '}';
    }
}
