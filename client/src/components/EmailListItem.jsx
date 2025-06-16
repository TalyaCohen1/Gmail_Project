import React from 'react';
import '../styles/EmailListItem.css';

export default function EmailListItem({ email, isSelected, onToggleSelect, onDelete }) {
    return (
        <li className={`email-item ${isSelected ? 'selected' : ''}`}>  
        <input
            type="checkbox"
            className="email-checkbox"
            checked={isSelected}
            onChange={onToggleSelect}
        />
        <div className="email-summary">
            <span className="sender">{email.from}</span>
            <span className="subject">{email.subject}</span>
            <span className="date">{new Date(email.date).toLocaleString()}</span>
        </div>
        <div className="labels">
            {(email.labels || []).map(label => (
          <span key={label.id} className="tag">{label.name}</span>
        ))}
        </div>
        <button onClick={onDelete}>Delete</button>
        
        </li>
    );
}