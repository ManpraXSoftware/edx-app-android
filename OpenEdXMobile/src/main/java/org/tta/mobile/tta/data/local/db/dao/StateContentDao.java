package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.StateContent;

import java.util.List;

@Dao
public interface StateContentDao {

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("Select * from content inner join state_content " +
            "on content.id = state_content.content_id " +
            "where content.source_id = :sourceId ")
    List<Content> getAllContents(long sourceId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<StateContent> stateContents);

    @Query("Delete from state_content")
    void deleteAll();

    @Query("Delete from state_content where content_source_id = :sourceId")
    void deleteAllBySourceId(long sourceId);

}
