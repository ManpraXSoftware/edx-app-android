package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.Section;

import java.util.List;

@Dao
public interface SectionDao {

    @Query("Select * from section where username = :username")
    List<Section> getAll(String username);

    @Query("Select * from section where id = :id")
    Section getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Section> programs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Section program);

}
