package org.tta.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "category")
public class Category implements Comparable<Category>, Parcelable
{
    private long created_by;

    @PrimaryKey
    private long id;

    private String icon;

    private long source_id;

    private long order;

    private long modified_by;

    private String name;

    private String created_at;

    private String modified_at;

    public Category() {
    }

    protected Category(Parcel in) {
        created_by = in.readLong();
        id = in.readLong();
        icon = in.readString();
        source_id = in.readLong();
        order = in.readLong();
        modified_by = in.readLong();
        name = in.readString();
        created_at = in.readString();
        modified_at = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public long getCreated_by ()
    {
        return created_by;
    }

    public void setCreated_by (long created_by)
    {
        this.created_by = created_by;
    }

    public long getId ()
    {
        return id;
    }

    public void setId (long id)
    {
        this.id = id;
    }

    public String getIcon ()
    {
        return icon;
    }

    public void setIcon (String icon)
    {
        this.icon = icon;
    }

    public long getSource_id()
    {
        return source_id;
    }

    public void setSource_id(long source_id)
    {
        this.source_id = source_id;
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

    @Override
    public String toString()
    {
        return "Category [created_by = "+created_by+", id = "+id+", icon = "+icon+", source_id = "+ source_id +", order = "+order+", modified_by = "+modified_by+", name = "+name+", created_at = "+created_at+", modified_at = "+modified_at+"]";
    }

    @Override
    public int compareTo(Category o) {
        return Long.compare(order, o.order);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(created_by);
        dest.writeLong(id);
        dest.writeString(icon);
        dest.writeLong(source_id);
        dest.writeLong(order);
        dest.writeLong(modified_by);
        dest.writeString(name);
        dest.writeString(created_at);
        dest.writeString(modified_at);
    }
}
