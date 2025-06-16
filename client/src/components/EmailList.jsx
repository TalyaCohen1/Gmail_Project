// src/components/EmailList.jsx

import React from 'react';
import EmailListItem from './EmailListItem';
import '../styles/EmailList.css';

export default function EmailList({ emails = [], selectedIds, onToggleSelect, onDelete , onOpenEmail }) {
    if (emails.length === 0) {
        return (
        <div className="email-list-empty">
            <p>No emails to display.</p>
        </div>
        );
    }

    return (
        <ul className="email-list">
            {emails.map(email => (
                <EmailListItem
                    key={email.id}
                    email={email}
                    isSelected={selectedIds.includes(email.id)}
                    onToggleSelect={() => onToggleSelect(email.id)}
                    onDelete={() => onDelete(email.id)}
                    onOpenEmail={onOpenEmail} // Pass the onOpenEmail function
                    />
            ))}
        </ul>
    );
}