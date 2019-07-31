package org.tta.mobile.tta.data.local.db.table;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "bookmark",
        foreignKeys = {@ForeignKey(
                entity = Content.class,
                parentColumns = "id",
                childColumns = "content_id",
                onDelete = ForeignKey.CASCADE
        )}
)
public class Bookmark {

    @PrimaryKey
    @ColumnInfo(name = "content_id")
    private long contentId;

    @ColumnInfo(name = "content_source_id")
    private long sourceId;

    public Bookmark() {
    }

    public Bookmark(long contentId) {
        this.contentId = contentId;
    }

    @Ignore
    public Bookmark(long contentId, long sourceId) {
        this.contentId = contentId;
        this.sourceId = sourceId;
    }

    public long getContentId() {
        return contentId;
    }

    public void setContentId(long contentId) {
        this.contentId = contentId;
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }
}
