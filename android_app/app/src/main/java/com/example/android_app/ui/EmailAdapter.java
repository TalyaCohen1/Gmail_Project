package com.example.android_app.ui;

import android.content.Context;
import android.content.Intent;
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

    private List<Email> emailList;
    private Context context; // נצטרך את ה-Context כדי לפתוח Activity חדש

    public EmailAdapter(Context context, List<Email> emailList) {
        this.context = context;
        this.emailList = emailList;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יוצר את התצוגה עבור כל שורה ברשימה
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        // מקשר את הנתונים לתצוגה
        Email currentEmail = emailList.get(position);
        holder.bind(currentEmail);
    }

    @Override
    public int getItemCount() {
        return emailList != null ? emailList.size() : 0;
    }

    // מתודה לעדכון הרשימה כשהנתונים מגיעים מה-ViewModel
    public void setEmails(List<Email> emails) {
        this.emailList = emails;
        notifyDataSetChanged(); // מרענן את התצוגה
    }

    // ה-ViewHolder שמחזיק את הרכיבים של כל שורה
    class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView subjectTextView;
        // אפשר להוסיף גם תאריך, תצוגה מקדימה וכו'

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Email clickedEmail = emailList.get(position);

                    // יוצרים Intent כדי לפתוח את EmailDetailsActivity
                    Intent intent = new Intent(context, EmailDetailsActivity.class);
                    intent.putExtra("email_id", clickedEmail.getId());

                    // start the activity of email display
                    context.startActivity(intent);
                }
            });
        }

        void bind(Email email) {
            senderTextView.setText(email.getFrom());
            subjectTextView.setText(email.getSubject());
            // עדכני שדות נוספים אם יש
        }
    }
}