package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.Content;

import java.util.List;

@Dao
public interface ContentDao {

    @Query("Select * from content")
    List<Content> getAll();

    @Query("Select * from content where source_id = :sourceId " +
            "limit :take offset (:take*:skip)")
    List<Content> getAllBySourceId(long sourceId, int take, int skip);

    @Query("Select * from content limit :take offset (:take*:skip)")
    List<Content> getAll(int take, int skip);

    @Query("Select * from content where id = :id")
    Content getById(long id);

    @Query("Select * from content where source_identity = :sourceIdentity")
    Content getBySourceIdentity(String sourceIdentity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Content content);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertOrIgnore(Content content);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Content> contents);
}
