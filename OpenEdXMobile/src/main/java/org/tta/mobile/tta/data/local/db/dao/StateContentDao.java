package org.tta.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.StateContent;

import java.util.List;

@Dao
public interface StateContentDao {

    @Query("Select * from content inner join state_content " +
            "on content.id = state_content.content_id " +
            "where content.source_id = :sourceId ")
    List<Content> getAllContents(long sourceId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<StateContent> stateContents);

    @Query("Delete from state_content")
    void deleteAll();

}
