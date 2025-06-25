package com.example.android_app.model;

public class Email {
    public String id;
    public String Sender;
    public String receiver;
    public String subject;
    public String body;
    public long timestamp;
    public String date;

    public Email() { }

    public String getId() {
        return id;
    }

    public String getSender() {
        return Sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.Sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
