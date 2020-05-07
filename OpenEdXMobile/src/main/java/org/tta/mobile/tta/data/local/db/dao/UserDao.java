package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.tta.data.local.db.table.User;

import java.util.List;

@Dao
public interface  UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

}
