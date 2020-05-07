package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("Select * from category")
    List<Category> getAll();

    @Query("Select * from category where id = :id")
    Category getById(long id);

    @Query("Select * from category where source_id = :sourceId")
    Category getBySourceId(long sourceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Category> categories);
}
