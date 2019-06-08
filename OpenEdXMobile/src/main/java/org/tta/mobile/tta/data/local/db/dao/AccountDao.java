package org.tta.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tta.mobile.user.Account;

@Dao
public interface AccountDao {

    @Query("Select * from account where username = :username")
    Account getByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

}
