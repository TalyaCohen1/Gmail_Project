package com.example.android_app.data.local;
import androidx.room.*;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE _id = :id LIMIT 1")
    UserEntity getUserById(String id);

    @Query("SELECT * FROM users WHERE emailAddress = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Update
    void updateUser(UserEntity user);

    @Delete
    void deleteUser(UserEntity user);
}
