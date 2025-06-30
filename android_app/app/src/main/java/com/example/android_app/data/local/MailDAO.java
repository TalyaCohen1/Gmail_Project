package com.example.android_app.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface MailDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMail(MailEntity mail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MailEntity> mails);

    @Query("SELECT * FROM mails WHERE send = 0")
    LiveData<List<MailEntity>> getDrafts();

    @Query("SELECT * FROM mails WHERE send = 1")
    LiveData<List<MailEntity>> getSentMails();

    @Query("DELETE FROM mails")
    void clearAll();

    @Delete
    void deleteMail(MailEntity mail);
}
