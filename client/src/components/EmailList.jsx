import React from 'react';
import EmailListItem from './EmailListItem';
import '../style/EmailList.css';


export default function EmailList({ emails, selectedIds, onToggleSelect, onDelete }) {
    return (
        <ul className="email-list">
        {emails.map(email => (
            <EmailListItem
            key={email.id}
            email={email}
            isSelected={selectedIds.includes(email.id)}
            onToggleSelect={() => onToggleSelect(email.id)}
            onDelete={() => onDelete(email.id)}
            />
        ))}
        </ul>
    );
}