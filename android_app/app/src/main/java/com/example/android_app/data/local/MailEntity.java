package com.example.android_app.data.local;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;

// This class represents the Mail entity in the local database.
@Entity(tableName = "mails")
public class MailEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String from;
    public String to;
    public String subject;
    public String body;
    public String date;
    public long timestamp;
    public boolean send;
    public boolean isSpam;
    public boolean isRead;
    public boolean isImportant;
    public boolean isStarred;
    public boolean deletedForSender;
    public boolean deletedForReceiver;

    // for object from type List<ObjectId>
    @TypeConverters(LabelIdConverter.class)
    public List<String> labelsForSender;

    @TypeConverters(LabelIdConverter.class)
    public List<String> labelsForReceiver;

    public Object getId() {
        return id;
    }
}