package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.CurricullamChaptersModel;

import java.util.List;


@Dao
public interface CurricullamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(CurricullamChaptersModel chapter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<CurricullamChaptersModel> chapters);

    @Query("Select * from curriculam where userName =:url")
    List<CurricullamChaptersModel> getAllChapters(String url);
}
