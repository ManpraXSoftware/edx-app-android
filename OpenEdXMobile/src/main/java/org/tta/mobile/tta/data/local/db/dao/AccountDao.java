package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.tta.mobile.user.Account;

@Dao
public interface AccountDao {

    @Query("Select * from account where username = :username")
    Account getByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

}
