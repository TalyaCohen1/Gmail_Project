package com.example.android_app.model;

public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private boolean send;

    public EmailRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.send = true; // Default to sending the email
    }
    public EmailRequest(String to, String subject, String body, boolean send) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.send = send; // Allows setting 'send' explicitly
    }

    // Getters & Setters
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
        }
    public String getBody() {
        return body;
    }
    public void setBody(String body) { this.body = body; }
    public boolean isSend() { // Getter for 'send'
        return send;
    }

    public void setSend(boolean send) { // Setter for 'send'
        this.send = send;
    }

}

