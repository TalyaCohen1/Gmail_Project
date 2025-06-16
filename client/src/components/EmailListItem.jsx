import React from 'react';
import '../styles/EmailListItem.css';
import{ useNavigate} from 'react-router-dom';

export default function EmailListItem({ email, isSelected, onToggleSelect, onDelete, onOpenEmail }) {
    const navigate = useNavigate();

     return (
        <li 
          className={`email-item ${isSelected ? 'selected' : ''}`}  
          onClick={() => onOpenEmail(email)}
          style={{ cursor: 'pointer' }}
        >
            <input
                type="checkbox"
                className="email-checkbox"
                checked={isSelected}
                onClick={(e) => e.stopPropagation()}  // Prevent checkbox click from triggering the item click
                onChange={() => onToggleSelect(email.id)}
            
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
            <button onClick={e => {
                e.stopPropagation();
                onDelete(email.id);
            }}>Delete</button>
        </li>
    );
}