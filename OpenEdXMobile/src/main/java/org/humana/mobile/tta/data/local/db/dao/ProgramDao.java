package org.humana.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.humana.mobile.tta.data.local.db.table.Program;

import java.util.List;

@Dao
public interface ProgramDao {

    @Query("Select * from program where username = :username")
    List<Program> getAll(String username);

    @Query("Select * from program where id = :id")
    Program getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Program> programs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Program program);

}
