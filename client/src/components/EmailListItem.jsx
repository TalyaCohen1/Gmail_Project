import React from 'react';
import '../styles/EmailListItem.css';
import{ useNavigate} from 'react-router-dom';

export default function EmailListItem({ email, isSelected, onToggleSelect, onDelete, onOpenEmail }) {
     return (
        <li 
          className={`email-item ${isSelected ? 'selected' : ''}`}  
          onClick={() => onOpenEmail(email)}
          style={{ cursor: 'pointer' }}
        >
        <div className="col col-checkbox">
            <input
                type="checkbox"
                className="email-checkbox"
                checked={isSelected}
                onClick={(e) => e.stopPropagation()}  // Prevent checkbox click from triggering the item click
                onChange={() => onToggleSelect(email.id)}
            
            />
        </div>
            <div className="col col-subject">
                <span className="subject-text">{email.subject}</span>
                <span className="snippet-text"> - {email.body?.slice(0, 50)}...</span>
            </div>

            <div className="col col-date">
                {new Date(email.date).toLocaleString()}
            </div>
            <div className="labels">
                {(email.labels || []).map(label => (
                  <span key={label.id} className="tag">{label.name}</span>
                ))}
            </div>
            <button onClick={e => {
                e.stopPropagation();
                onDelete(email.id);
            }}>Delete</button>
        </li>
    );
}