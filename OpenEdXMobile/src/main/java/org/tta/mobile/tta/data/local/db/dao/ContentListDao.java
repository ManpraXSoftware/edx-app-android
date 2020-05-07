package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.ContentList;

import java.util.List;

@Dao
public interface ContentListDao {

    @Query("Select * from content_list")
    List<ContentList> getAll();

    @Query("Select * from content_list where id = :id")
    ContentList getById(long id);

    @Query("Select * from content_list where category_id = :categoryId")
    List<ContentList> getAllByCategoryId(long categoryId);

    @Query("Select * from content_list where category_id = :categoryId and mode = :mode")
    List<ContentList> getAllByCategoryIdAndMode(long categoryId, String mode);

    @Query("Select * from content_list where root_category = :rootCategory")
    List<ContentList> getByRootCategory(String rootCategory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContentList contentList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<ContentList> contentLists);

    @Query("Delete from content_list")
    void deleteAll();

}
