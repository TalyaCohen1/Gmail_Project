package com.example.android_app.model;

public class Email {
    public String id;
    public String from;
    public String to;
    public String subject;
    public String body;
    public long timestamp;
    public String date;

    public Email(String id, String from, String to, String subject, String body, long timestamp, String date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
        this.date = date;
    }
}
