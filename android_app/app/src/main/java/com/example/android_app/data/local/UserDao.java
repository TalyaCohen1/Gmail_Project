package com.example.android_app.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM mails ORDER BY timestamp DESC")
    LiveData<List<MailEntity>> getAllMails();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE _id = :id LIMIT 1")
    UserEntity getUserById(String id);

    @Update
    void updateUser(UserEntity user);

    @Delete
    void deleteUser(UserEntity user);
}
