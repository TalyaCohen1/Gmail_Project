import React, { useState, useEffect } from 'react';
import '../styles/EmailListItem.css';
import{ useNavigate} from 'react-router-dom';

export default function EmailListItem({ 
    email, 
    isSelected, 
    onToggleSelect, 
    onDelete, 
    onToggleStar,
    onToggleImportant, 
    onToggleRead, 
    onOpenEmail }) {

    const navigate = useNavigate();

// import { ReactComponent as starred }       from '/icons/starred.svg';
// import { ReactComponent as full_star }     from '/icons/full_star.svg';
// import { ReactComponent as Important }    from '/icons/important.svg';
// import { ReactComponent as UnImportant }  from '/icons/full_important.svg';
// import { ReactComponent as MarkAsRead }   from '/icons/mark_as_read.svg';
// import { ReactComponent as MarkAsUnread } from '/icons/mark_as_unread.svg';
// import { ReactComponent as Trash }        from '/icons/trash.svg'; {
  // local UI state
  const [starredState, setStarredState]         = useState(!!email.starred);
  const [importantState, setImportantState]     = useState(!!email.important);
  const [readState, setReadState]               = useState(!!email.read);

  // update local state when props change
  useEffect(() => { setStarredState(!!email.starred);   }, [email.starred]);
  useEffect(() => { setImportantState(!!email.important); }, [email.important]);
  useEffect(() => { setReadState(!!email.read);         }, [email.read]);

  // handle user actions
  const handleStar = () => {
    const next = !starredState;
    setStarredState(next);
    onToggleStar(email.id, next);
  };

  const handleImportant = () => {
    const next = !importantState;
    setImportantState(next);
    onToggleImportant(email.id, next);
  };

  const handleRead = () => {
    const next = !readState;
    setReadState(next);
    onToggleRead(email.id, next);
  };

  
  // choose icons
//   const StarIcon       = starredState ? full_star : star;
//   const ImportantIcon  = importantState ? UnImportant : Important;
//   const ReadIcon       = readState ? MarkAsUnread : MarkAsRead;

  return (
    <li
      className={`email-item ${isSelected ? 'selected' : ''} ${readState ? 'read' : 'unread'}`}
      onClick={handleItemClick}
      style={{ cursor: 'pointer' }}
    >
      <div className="email-item-left">
        <input
          type="checkbox"
          className="email-checkbox"
          checked={isSelected}
          onChange={e => { e.stopPropagation(); onToggleSelect(); }}
        />

        <button className="icon-btn" onClick={handleStar} title={starredState ? 'Unstar' : 'Star'}>
          <img
            src={`/icons/${starredState ? 'full_star' : 'starred'}.svg`}
            alt={starredState ? 'Unstar' : 'Star'}
            className="inline-icon"
          />
        </button>

        <button className="icon-btn" onClick={handleImportant} title={importantState ? 'Unmark Important' : 'Important'}>
          <img
            src={`/icons/${importantState ? 'full_important' : 'important'}.svg`}
            alt={importantState ? 'Unmark Important' : 'Important'}
            className="inline-icon"
          />
        </button>
      </div>

      <div className="email-summary" onClick={handleReadToggle}>
        <span className="sender">{email.from}</span>
        <span className="subject">{email.subject}</span>
        <span className="date">{new Date(email.timestamp).toLocaleString()}</span>
      </div>

      <div className="labels">
        {(email.labels || []).map(label => (
          <span key={label.id} className="tag">{label.name}</span>
        ))}
      </div>

      <div className="email-item-right">
        <button className="icon-btn" onClick={handleReadToggle} title={readState ? 'Mark as Unread' : 'Mark as Read'}>
          <img
            src={`/icons/${readState ? 'mark_as_unread' : 'mark_as_read'}.svg`}
            alt={readState ? 'Mark as Unread' : 'Mark as Read'}
            className="inline-icon"
          />
        </button>

        <button className="icon-btn" onClick={handleSnooze} title="Snooze">
          <img
            src={`/icons/snooze.svg`}
            alt="Snooze"
            className="inline-icon"
          />
        </button>

        <button className="icon-btn" onClick={handleDeleteClick} title="Delete">
          <img
            src={`/icons/trash.svg`}
            alt="Delete"
            className="inline-icon"
          />
        </button>
      </div>
    </li>
  );
}


// export default function EmailListItem({ email, isSelected, onToggleSelect, onDelete }) {
//     return (
//         <li className={`email-item ${isSelected ? 'selected' : ''}`}>  
//         <input
//             type="checkbox"
//             className="email-checkbox"
//             checked={isSelected}
//             onChange={onToggleSelect}
//         />
//         <div className="email-summary">
//             <span className="sender">{email.from}</span>
//             <span className="subject">{email.subject}</span>
//             <span className="date">{new Date(email.date).toLocaleString()}</span>
//         </div>
//         <div className="labels">
//             {(email.labels || []).map(label => (
//           <span key={label.id} className="tag">{label.name}</span>
//         ))}
//         </div>
//         <button onClick={onDelete}>Delete</button>
        
//         </li>
//     );
// }