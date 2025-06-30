package com.example.android_app.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;


@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String _id;

    public String fullName;
    public String emailAddress;
    public String birthDate;
    public String gender;
    public String password;
    public String profileImage;
    public Long createdAt;
    public Long updatedAt;

    public UserEntity(String _id, String fullName, String emailAddress, String birthDate, String gender, String password, String profileImage, Long createdAt, Long updatedAt) {
        this._id = _id;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.birthDate = birthDate;
        this.gender = gender;
        this.password = password;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
