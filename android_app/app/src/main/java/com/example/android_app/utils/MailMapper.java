package com.example.android_app.utils;

import com.example.android_app.model.Email;
import com.example.android_app.data.local.MailEntity;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Locale;

//this class use to switch between the two model
public class MailMapper {
    public static MailEntity toEntity(Email email) {
        MailEntity entity = new MailEntity();

        entity.id = email.getId();
        entity.from = email.getFrom();
        entity.to = email.getTo();
        entity.subject = email.getSubject();
        entity.body = email.getBody();
        entity.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(email.getDate());
        entity.timestamp = email.getTimestamp();
        entity.send = email.isSend();
        entity.isRead = email.getIsRead();
        entity.isSpam = email.isSpam();
        entity.isImportant = email.isImportant();
        entity.isStarred = email.isStarred();

        //default values
        entity.deletedForSender = false;
        entity.deletedForReceiver = false;
        entity.labelsForSender = new ArrayList<>();
        entity.labelsForReceiver = new ArrayList<>();

        return entity;
    }

    public static Email toEmail(MailEntity entity) {
        Email email = new Email();

        email.setId(entity.id);
        email.setSender(entity.from);
        email.setReceiver(entity.to);
        email.setSubject(entity.subject);
        email.setBody(entity.body);
        email.setDate(entity.date);
        email.setTimestamp(entity.timestamp);
        email.setSend(entity.send);
        email.setIsRead(entity.isRead);
        // if (!entity.isRead) email.setIsRead();
        email.setSpam(entity.isSpam);
        email.setImportant(entity.isImportant);
        email.setStarred(entity.isStarred);

        return email;
    }

    public static List<MailEntity> toEntities(List<Email> emails) {
        List<MailEntity> result = new ArrayList<>();
        for (Email email : emails) {
            result.add(toEntity(email));
        }
        return result;
    }

    public static List<Email> toEmails(List<MailEntity> entities) {
        List<Email> result = new ArrayList<>();
        for (MailEntity entity : entities) {
            result.add(toEmail(entity));
        }
        return result;
    }

}
