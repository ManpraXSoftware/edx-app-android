package org.tta.mobile.tta.data.local.db.table;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

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

    public StateContent() {
    }

    public StateContent(long contentId) {
        this.contentId = contentId;
    }

    public long getContentId() {
        return contentId;
    }

    public void setContentId(long contentId) {
        this.contentId = contentId;
    }

}
