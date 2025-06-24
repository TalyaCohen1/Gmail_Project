package com.example.android_app.model;

public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private boolean send = true;

    public EmailRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    // Getters & Setters
}

