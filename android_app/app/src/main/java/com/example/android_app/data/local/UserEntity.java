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
    public String profileImage;
    public String emailAddress;

    public UserEntity(String _id, String fullName, String profileImage, String emailAddress) {
        this._id = _id;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
