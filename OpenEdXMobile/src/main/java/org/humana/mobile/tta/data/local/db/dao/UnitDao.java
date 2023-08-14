package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.Unit;

import java.util.List;

@Dao
public interface UnitDao {

    @Query("Select * from unit " +
            "where programId = :programId and sectionId = :sectionId " +
            "limit :take offset (:take*:skip)")
    List<Unit> getAll(String programId, String sectionId, int take, int skip);

    @Query("Select * from unit where id = :id")
    Unit getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Unit> units);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Unit unit);

}
