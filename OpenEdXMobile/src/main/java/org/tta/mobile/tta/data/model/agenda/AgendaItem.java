package org.tta.mobile.tta.data.model.agenda;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class AgendaItem implements Comparable<AgendaItem>, Parcelable {

    private long source_id;

    private long content_count;

    private String source_name;

    private String source_title;

    private String source_icon;

    private long order;

    public AgendaItem() {
    }

    protected AgendaItem(Parcel in) {
        source_id = in.readLong();
        content_count = in.readLong();
        source_name = in.readString();
        source_title = in.readString();
        source_icon = in.readString();
        order = in.readLong();
    }

    public static final Creator<AgendaItem> CREATOR = new Creator<AgendaItem>() {
        @Override
        public AgendaItem createFromParcel(Parcel in) {
            return new AgendaItem(in);
        }

        @Override
        public AgendaItem[] newArray(int size) {
            return new AgendaItem[size];
        }
    };

    public long getSource_id() {
        return source_id;
    }

    public void setSource_id(long source_id) {
        this.source_id = source_id;
    }

    public long getContent_count() {
        return content_count;
    }

    public void setContent_count(long content_count) {
        this.content_count = content_count;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getSource_title() {
        return source_title;
    }

    public void setSource_title(String source_title) {
        this.source_title = source_title;
    }

    public String getSource_icon() {
        return source_icon;
    }

    public void setSource_icon(String source_icon) {
        this.source_icon = source_icon;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof AgendaItem) && source_name.equalsIgnoreCase(((AgendaItem) obj).source_name);
    }

    @Override
    public int compareTo(AgendaItem o) {
        return Long.compare(order, o.order);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(source_id);
        dest.writeLong(content_count);
        dest.writeString(source_name);
        dest.writeString(source_title);
        dest.writeString(source_icon);
        dest.writeLong(order);
    }
}
