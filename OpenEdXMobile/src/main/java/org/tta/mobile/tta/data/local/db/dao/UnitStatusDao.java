package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.UnitStatus;

import java.util.List;

@Dao
public interface UnitStatusDao {

    @Query("Select * from unit_status where username = :username and course_id = :courseId")
    List<UnitStatus> getAllByCourse(String username, String courseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<UnitStatus> statuses);

}
