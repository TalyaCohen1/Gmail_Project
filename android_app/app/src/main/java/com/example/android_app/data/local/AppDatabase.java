package com.example.android_app.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {MailEntity.class, UserEntity.class}, version = 3)
@TypeConverters(LabelIdConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract MailDAO mailDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "mail_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
