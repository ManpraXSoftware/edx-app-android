package org.tta.mobile.tta.data.model.agenda;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AgendaList implements Parcelable {

    private String level;

    private List<AgendaItem> result;

    private long list_id;

    public AgendaList() {
    }

    protected AgendaList(Parcel in) {
        level = in.readString();
        result = in.createTypedArrayList(AgendaItem.CREATOR);
        list_id = in.readLong();
    }

    public static final Creator<AgendaList> CREATOR = new Creator<AgendaList>() {
        @Override
        public AgendaList createFromParcel(Parcel in) {
            return new AgendaList(in);
        }

        @Override
        public AgendaList[] newArray(int size) {
            return new AgendaList[size];
        }
    };

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<AgendaItem> getResult() {
        return result;
    }

    public void setResult(List<AgendaItem> result) {
        this.result = result;
    }

    public long getList_id() {
        return list_id;
    }

    public void setList_id(long list_id) {
        this.list_id = list_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(level);
        dest.writeTypedList(result);
        dest.writeLong(list_id);
    }
}
