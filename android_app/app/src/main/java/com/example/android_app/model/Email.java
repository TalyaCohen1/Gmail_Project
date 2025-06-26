package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

public class Email {
    @SerializedName("_id") // או "mail_id" או מה שזה לא יהיה בשרת
    private String id;

    @SerializedName("from") // או "sender_email", "sender"
    private String from;

    @SerializedName("to")
    private String to;

    @SerializedName("subject") // או "mail_subject"
    private String subject;

    @SerializedName("body") // או "content"
    private String body;

    @SerializedName("date") // או "sent_at"
    private String date;

    @SerializedName("isRead") // או "is_read"
    private boolean isRead;

    @SerializedName("send")
    private boolean send;

    @SerializedName("isSpam")
    private boolean isSpam;

    @SerializedName("isImportant")
    private boolean isImportant;

    @SerializedName("isStarred")
    private boolean isStarred;

    @SerializedName("timestamp")
    private long timestamp;

    public Email() { }

    // Getters
    public String getId() { return id; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getDate() { return date; }
    public boolean isRead() { return isRead; }

    public void setId(String id) {
        this.id = id;
    }
    public void setSender(String sender) {
        this.from = sender;
    }
    public void setReceiver(String receiver) {
        this.to = receiver;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setIsRead() {
        isRead = !isRead;
    }
    public Boolean getIsRead() {
        return isRead;
    }

    public boolean isImportant() {
        return isImportant;
    }
    public void setImportant(boolean important) {
        isImportant = important;
    }
    public boolean isStarred() {
        return isStarred;
    }
    public void setStarred(boolean starred) {
        isStarred = starred;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public boolean isSend() {
        return send;
    }
    public void setSend(boolean send) {
        this.send = send;
    }
    public boolean isSpam() {
        return isSpam;
    }
    public void setSpam(boolean spam) {
        isSpam = spam;
    }
}
