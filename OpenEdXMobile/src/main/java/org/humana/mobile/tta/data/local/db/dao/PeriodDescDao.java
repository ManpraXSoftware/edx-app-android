package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;

import java.util.List;
@Dao
public interface PeriodDescDao {
    @Query("Select * from periodDesc where about_url = :about_url")
    List<DownloadPeriodDesc> getAll(String about_url);

    @Query("Select * from periodDesc where id = :id")
    DownloadPeriodDesc getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadPeriodDesc periodDesc);
}
