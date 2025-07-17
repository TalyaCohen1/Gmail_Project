package com.example.android_app.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

// This class represents the Mail entity in the local database - it contains the email details and query methods for accessing the emails.
@Dao
public interface MailDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMail(MailEntity mail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MailEntity> mails);

    @Query("SELECT * FROM mails ORDER BY timestamp DESC")
    LiveData<List<MailEntity>> getAllMails();

    @Query("SELECT * FROM mails ORDER BY timestamp DESC")
    List<MailEntity> getAllMailsNow(); //without LiveData


    @Query("SELECT * FROM mails WHERE send = 0")
    LiveData<List<MailEntity>> getDrafts();

    @Query("SELECT * FROM mails WHERE send = 1")
    LiveData<List<MailEntity>> getSentMails();

    @Query("DELETE FROM mails")
    void clearAll();

    @Delete
    void deleteMail(MailEntity mail);

    @Query("SELECT * FROM mails WHERE id = :id")
    LiveData<MailEntity> getMailById(int id);

    // added: quick getter for MailEntity
    @Query("SELECT * FROM mails WHERE id = :mailId")
    MailEntity getMailByIdNow(String mailId);
}
