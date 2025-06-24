package com.example.android_app.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.model.Email;

import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {
    private final List<Email> emailList;

    public EmailAdapter(List<Email> emails) {
        this.emailList = emails;
    }

    public static class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textSubject, textTime, textAvatar;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.textSender);
            textSubject = itemView.findViewById(R.id.textSubject);
            textTime = itemView.findViewById(R.id.textTime);
            textAvatar = itemView.findViewById(R.id.textAvatar);
        }
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email email = emailList.get(position);

        holder.textSender.setText(email.from);
        holder.textSubject.setText(email.subject);
        holder.textTime.setText("10:45"); // את יכולה להמיר timestamp לזמן אמיתי
        holder.textAvatar.setText(email.from.substring(0, 1).toUpperCase());

        // כשהמשתמש לוחץ על פריט ברשימה
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EmailDetailsActivity.class);
            intent.putExtra("email_id", email.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return emailList.size();
    }
}
