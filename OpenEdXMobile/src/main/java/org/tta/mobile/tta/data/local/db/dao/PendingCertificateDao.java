package org.tta.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.tta.mobile.tta.data.local.db.table.PendingCertificate;

import java.util.List;

@Dao
public interface PendingCertificateDao {

    @Query("Select * from pending_certificate where username = :username")
    List<PendingCertificate> getAll(String username);

    @Query("Select * from pending_certificate where course_id = :courseId and username = :username")
    PendingCertificate getByCourseId(String courseId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingCertificate certificate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<PendingCertificate> certificates);

    @Delete
    void delete(PendingCertificate certificate);

    @Query("Delete from pending_certificate where username = :username")
    void deleteAll(String username);

    @Query("Delete from pending_certificate where username = :username and course_id = :courseId")
    void deleteByCourseId(String username, String courseId);

}
