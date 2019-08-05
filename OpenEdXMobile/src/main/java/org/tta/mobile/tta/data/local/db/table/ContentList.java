package org.tta.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

@Entity(tableName = "content_list")
public class ContentList implements Comparable<ContentList>, Parcelable
{
    private String region;

    private long created_by;

    private String sort_as;

    private String sort_by;

    private String mode;

    @PrimaryKey
    private long id;

    private String auto_function;

    private long category_id;

    private long order;

    private long modified_by;

    private String name;

    private String internal_name;

    private String format_type;

    private String created_at;

    private String modified_at;

    private String root_category;

    public ContentList() {
    }

    protected ContentList(Parcel in) {
        region = in.readString();
        created_by = in.readLong();
        sort_as = in.readString();
        sort_by = in.readString();
        mode = in.readString();
        id = in.readLong();
        auto_function = in.readString();
        category_id = in.readLong();
        order = in.readLong();
        modified_by = in.readLong();
        name = in.readString();
        internal_name = in.readString();
        format_type = in.readString();
        created_at = in.readString();
        modified_at = in.readString();
        root_category = in.readString();
    }

    public static final Creator<ContentList> CREATOR = new Creator<ContentList>() {
        @Override
        public ContentList createFromParcel(Parcel in) {
            return new ContentList(in);
        }

        @Override
        public ContentList[] newArray(int size) {
            return new ContentList[size];
        }
    };

    public String getRegion ()
    {
        return region;
    }

    public void setRegion (String region)
    {
        this.region = region;
    }

    public long getCreated_by ()
    {
        return created_by;
    }

    public void setCreated_by (long created_by)
    {
        this.created_by = created_by;
    }

    public String getSort_as ()
    {
        return sort_as;
    }

    public void setSort_as (String sort_as)
    {
        this.sort_as = sort_as;
    }

    public String getSort_by ()
    {
        return sort_by;
    }

    public void setSort_by (String sort_by)
    {
        this.sort_by = sort_by;
    }

    public String getMode ()
    {
        return mode;
    }

    public void setMode (String mode)
    {
        this.mode = mode;
    }

    public long getId ()
    {
        return id;
    }

    public void setId (long id)
    {
        this.id = id;
    }

    public String getAuto_function ()
    {
        return auto_function;
    }

    public void setAuto_function (String auto_function)
    {
        this.auto_function = auto_function;
    }

    public long getCategory_id()
    {
        return category_id;
    }

    public void setCategory_id(long category_id)
    {
        this.category_id = category_id;
    }

    public long getOrder ()
    {
        return order;
    }

    public void setOrder (long order)
    {
        this.order = order;
    }

    public long getModified_by ()
    {
        return modified_by;
    }

    public void setModified_by (long modified_by)
    {
        this.modified_by = modified_by;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getInternal_name() {
        return internal_name == null ? "content_list" : internal_name;
    }

    public void setInternal_name(String internal_name) {
        this.internal_name = internal_name;
    }

    public String getFormat_type ()
    {
        return format_type;
    }

    public void setFormat_type (String format_type)
    {
        this.format_type = format_type;
    }

    public String getCreated_at ()
    {
        return created_at;
    }

    public void setCreated_at (String created_at)
    {
        this.created_at = created_at;
    }

    public String getModified_at ()
    {
        return modified_at;
    }

    public void setModified_at (String modified_at)
    {
        this.modified_at = modified_at;
    }

    public String getRoot_category() {
        return root_category;
    }

    public void setRoot_category(String root_category) {
        this.root_category = root_category;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int compareTo(ContentList o) {
        return Long.compare(order, o.order);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof ContentList) && (((ContentList) obj).id == id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(region);
        dest.writeLong(created_by);
        dest.writeString(sort_as);
        dest.writeString(sort_by);
        dest.writeString(mode);
        dest.writeLong(id);
        dest.writeString(auto_function);
        dest.writeLong(category_id);
        dest.writeLong(order);
        dest.writeLong(modified_by);
        dest.writeString(name);
        dest.writeString(internal_name);
        dest.writeString(format_type);
        dest.writeString(created_at);
        dest.writeString(modified_at);
        dest.writeString(root_category);
    }
}
