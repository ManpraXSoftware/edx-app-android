package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.Period;

import java.util.List;

@Dao
public interface PeriodDao {

    @Query("Select * from period where username = :username")
    List<Period> getAll(String username);

    @Query("Select * from period where id = :id")
    Period getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Period> programs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Period program);

}
