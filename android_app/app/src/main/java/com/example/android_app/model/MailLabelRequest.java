package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

public class MailLabelRequest {
    @SerializedName("mailId")
    private String mailId;

    public MailLabelRequest(String mailId) {
        this.mailId = mailId;
    }

    public String getMailId() {
        return mailId;
    }
}
