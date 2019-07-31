package org.tta.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;

import org.tta.mobile.tta.data.local.db.table.Bookmark;
import org.tta.mobile.tta.data.local.db.table.Content;

import java.util.List;

@Dao
public interface BookmarkDao {

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("Select * from content inner join bookmark " +
            "on content.id = bookmark.content_id " +
            "where content.source_id = :sourceId ")
    List<Content> getAllContents(long sourceId);

    @Query("Select * from bookmark where content_id = :contentId")
    Bookmark getByContentId(long contentId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Bookmark bookmark);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Bookmark> bookmarks);

    @Delete
    void delete(Bookmark bookmark);

    @Query("Delete from bookmark")
    void deleteAll();

    @Query("Delete from bookmark where content_source_id = :sourceId")
    void deleteAllBySourceId(long sourceId);

}
