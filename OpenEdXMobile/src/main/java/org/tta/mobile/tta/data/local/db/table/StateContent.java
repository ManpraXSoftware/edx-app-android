package org.tta.mobile.tta.data.local.db.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "state_content",
        foreignKeys = {@ForeignKey(
                entity = Content.class,
                parentColumns = "id",
                childColumns = "content_id",
                onDelete = ForeignKey.CASCADE
        )}
)
public class StateContent {

    @PrimaryKey
    @ColumnInfo(name = "content_id")
    private long contentId;

    @ColumnInfo(name = "content_source_id")
    private long sourceId;

    public StateContent() {
    }

    @Ignore
    public StateContent(long contentId, long sourceId) {
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
