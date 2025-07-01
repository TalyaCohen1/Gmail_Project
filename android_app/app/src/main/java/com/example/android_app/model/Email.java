package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public class Email {
    @SerializedName("_id") // או "mail_id" או מה שזה לא יהיה בשרת
    private String id;

    @SerializedName("from") // או "sender_email", "sender"
    private String from;

    private String senderName;

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
    //public String getDate() { return date; }
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
    public void setIsRead(boolean read) {
        isRead = read;
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

    public Date getDate() {
        if (date == null || date.isEmpty()) {
            return null;
        }
        // התאם את פורמט התאריך לפורמט שאת מקבלת מה-API
        // לדוגמה, אם הפורמט הוא "2025-06-29T19:12:26Z" (ISO 8601), השתמשי בזה:
        // SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        // parser.setTimeZone(TimeZone.getTimeZone("UTC")); // חשוב אם התאריך הוא ב-UTC

        // אם הפורמט שונה, תצטרכי לשנות את המחרוזת פה בהתאם
        // לדוגמה, אם זה "Jun 29, 2025 7:12:26 PM" אז "MMM dd, yyyy hh:mm:ss a"
        // עליך לדעת את הפורמט המדויק מהשרת!
        try {
            // נניח פורמט ISO 8601 עם אזור זמן (Z for UTC)
            // אם את משתמשת ב-API 26+, עדיף להשתמש ב-Instant/ZonedDateTime
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return Date.from(Instant.parse(date));
            } else {
                // Fallback for older APIs (needs exact string format)
                // זו דוגמה לפורמט אם יש לך רק תאריך ושעה ללא Timezone
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                return parser.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (java.time.format.DateTimeParseException e) { // For Instant.parse() errors
            e.printStackTrace();
            return null;
        }
    }
}

