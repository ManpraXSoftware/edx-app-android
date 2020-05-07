package org.tta.mobile.tta.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.tta.mobile.tta.data.local.db.table.Notification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Query("Select * from notification " +
            "where username = :username " +
            "order by created_time desc")
    List<Notification> getAll(String username);

    @Query("Select * from notification " +
            "where username = :username " +
            "order by created_time desc " +
            "limit :take offset (:take * :skip)")
    List<Notification> getAllInPage(String username, int take, int skip);

    @Query("Select * from notification " +
            "where username = :username and id = 0")
    List<Notification> getAllUncreated(String username);

    @Query("Select * from notification " +
            "where username = :username and id != 0 and updated = 0 and seen = 1")
    List<Notification> getAllUnupdated(String username);

    @Query("Select * from notification " +
            "where username = :username and id = :id")
    Notification getById(String username, long id);

    @Query("Select * from notification " +
            "where username = :username and created_time = :createdTime")
    Notification getByCreatedTime(String username, long createdTime);

    @Query("Select * from notification " +
            "where username = :username and local_id = :localId")
    Notification getByLocalId(String username, long localId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Notification> notification);

    @Query("Update notification set updated = 1 where id = :id")
    void updateNotification(long id);

    @Update
    void update(List<Notification> notifications);

    @Update
    void update(Notification notification);

}
