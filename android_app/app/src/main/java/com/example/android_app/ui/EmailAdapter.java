package com.example.android_app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.model.Email;

import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    private List<Email> emails;
    private OnEmailClickListener listener;

    // 1. הגדרת ה-interface לטיפול בלחיצה
    public interface OnEmailClickListener {
        void onEmailClick(Email email);
    }

    // 2. קונסטרוקטור שמקבל את המאזין
    public EmailAdapter(List<Email> emails, OnEmailClickListener listener) {
        this.emails = emails;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email currentEmail = emails.get(position);
        holder.bind(currentEmail, listener); // מעבירים את המייל והמאזין
    }

    @Override
    public int getItemCount() {
        return emails == null ? 0 : emails.size();
    }

    public void updateEmails(List<Email> newEmails) {
        this.emails = newEmails;
        notifyDataSetChanged();
    }

    // ViewHolder הוא קלאס פנימי סטטי
    public static class EmailViewHolder extends RecyclerView.ViewHolder {
        private TextView senderTextView;
        // ... שאר ה-TextViews

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.textViewSender);
            // ...
        }

        // 3. פונקציית ה-bind מקבלת את המאזין
        public void bind(final Email email, final OnEmailClickListener listener) {
            senderTextView.setText(email.getSender());
            // ...
            // 4. הגדרת הלחיצה על הפריט כולו
            itemView.setOnClickListener(v -> listener.onEmailClick(email));
        }
    }
}