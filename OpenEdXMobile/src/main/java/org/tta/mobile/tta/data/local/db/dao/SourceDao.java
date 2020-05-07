package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.Source;

import java.util.List;

@Dao
public interface SourceDao {

    @Query("Select * from source")
    List<Source> getAll();

    @Query("Select * from source where id = :id")
    Source getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Source source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Source> sources);

}
