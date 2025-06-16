import React, { useState, useEffect } from 'react';
import '../styles/EmailListItem.css';
import { useNavigate } from 'react-router-dom';

export default function EmailListItem({
  email,
  isSelected,
  onToggleSelect,
  onDelete,
  onToggleStar,
  onToggleImportant,
  onToggleRead,
  onOpenEmail,
}) {
  const navigate = useNavigate();
  
  // local UI state
//   const [starredState, setStarredState] = useState(!!email.starred);
//   const [importantState, setImportantState] = useState(!!email.important);
//   const [readState, setReadState] = useState(!!email.read);

//   // update local state when props change
//   useEffect(() => { setStarredState(!!email.starred); }, [email.starred]);
//   useEffect(() => { setImportantState(!!email.important); }, [email.important]);
//   useEffect(() => { setReadState(!!email.read); }, [email.read]);

  // handle user actions with stopPropagation to prevent email opening
  const handleStar = (e) => {
    e.stopPropagation();
    const next = !email.isStarred;
    //const next = !starredState;
    //setStarredState(next);
    onToggleStar(email.id, next);
  };

  const handleImportant = (e) => {
    e.stopPropagation();
    const next = !email.isImportant;
    //const next = !importantState;
    //setImportantState(next);
    onToggleImportant(email.id, next);
  };

  const handleRead = (e) => {
    e.stopPropagation();
    const next = !email.isRead;
    //const next = !readState;
    //setReadState(next);
    onToggleRead(email.id, next);
  };

  const handleDelete = (e) => {
    e.stopPropagation();
    onDelete(email.id);
  };

  const handleCheckboxChange = (e) => {
    e.stopPropagation();
    onToggleSelect(email.id);
  };

  return (
    <li 
      className={`email-item ${isSelected ? 'selected' : ''} ${email.isRead ? 'read' : 'unread'}`}
      onClick={() => onOpenEmail(email)}
      style={{ cursor: 'pointer' }}
    >
      <div className="email-item-left">
        <input
          type="checkbox"
          checked={isSelected}
          className="email-checkbox"
          onClick={(e) => e.stopPropagation()}  // Prevent checkbox click from triggering the item click
          onChange={() => onToggleSelect(email.id)}

        />

        <button className="icon-btn" onClick={handleStar} title="Star">
          <img
            src={`/icons/${email.isStarred ? 'full_star' : 'starred'}.svg`}
            alt={email.isStarred ? 'Unstar' : 'Star'}
            className="inline-icon"
          />
        </button>

        <button className="icon-btn" onClick={handleImportant} title="Important">
          <img
            src={`/icons/${email.isImportant ? 'full_important' : 'important'}.svg`}
            alt={email.isImportant ? 'Unmark important' : 'Important'}
            className="inline-icon"
          />
        </button>
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

      <div className="email-item-right">
        <button className="icon-btn" onClick={handleRead} title="Toggle Read">
          <img
            src={`/icons/${email.isRead ? 'mark_as_unread' : 'mark_as_read'}.svg`}
            alt={email.isRead ? 'Mark as unread' : 'Mark as read'}
            className="inline-icon"
          />
        </button>

        <button className="icon-btn" onClick={handleDelete} title="Delete">
          <img
            src={`${process.env.PUBLIC_URL}/icons/trash.svg`}
            alt="Delete"
            className="inline-icon"
          />
        </button>
      </div>
    </li>
  );
}