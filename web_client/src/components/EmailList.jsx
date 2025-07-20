// src/components/EmailList.jsx

import React from 'react';
import EmailListItem from './EmailListItem';
import '../styles/EmailList.css';

export default function EmailList({ emails = [], selectedIds, onToggleSelect, onDelete , onOpenEmail, onToggleStar, onToggleImportant, onToggleRead, onRefresh, currentView }) {
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
                    onToggleSelect={onToggleSelect}
                    onDelete={onDelete}
                    onOpenEmail={onOpenEmail}
                    onToggleStar={onToggleStar}
                    onToggleImportant={onToggleImportant}
                    onToggleRead={onToggleRead}
                    onRefresh={onRefresh}
                    currentView={currentView}
                />
            ))}
        </ul>
    );
}