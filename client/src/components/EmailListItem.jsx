import React from 'react';
import '../styles/EmailListItem.css';

import { ReactComponent as starred } from '../icons/starred.svg';
import { ReactComponent as full_star } from '../icons/full_star.svg';
import { ReactComponent as Important } from '../icons/important.svg';
import { ReactComponent as UnImportant } from '../icons/full_important.svg';
import { ReactComponent as SnoozeIcon } from '../icons/snooze.svg';
import { ReactComponent as MarkAsRead } from '../icons/mark_as_read.svg';
import { ReactComponent as MarkAsUnread } from '../icons/mark_as_unread.svg';
import { ReactComponent as Trash } from '../icons/trash.svg';

import React, { useState, useEffect } from 'react';
import '../styles/EmailListItem.css';

import { ReactComponent as starred }       from '../icons/starred.svg';
import { ReactComponent as full_star }     from '../icons/full_star.svg';
import { ReactComponent as Important }    from '../icons/important.svg';
import { ReactComponent as UnImportant }  from '../icons/full_important.svg';
import { ReactComponent as SnoozeIcon }   from '../icons/snooze.svg';
import { ReactComponent as MarkAsRead }   from '../icons/mark_as_read.svg';
import { ReactComponent as MarkAsUnread } from '../icons/mark_as_unread.svg';
import { ReactComponent as Trash }        from '../icons/trash.svg';

export default function EmailListItem({
  email,
  isSelected,
  onToggleSelect,
  onDelete,
  onToggleStar,
  onToggleImportant,
  onToggleRead,
  onSnooze
}) {
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

  const handleSnooze = () => {
    onSnooze(email.id);
  };

  // choose icons
  const StarIcon       = starredState ? full_star : starred;
  const ImportantIcon  = importantState ? UnImportant : Important;
  const ReadIcon       = readState ? MarkAsUnread : MarkAsRead;

  return (
    <li className={`email-item ${readState ? 'read' : 'unread'}`}>
      <div className="email-item-left">
        <input
          type="checkbox"
          checked={isSelected}
          onChange={onToggleSelect}
          className="email-checkbox"
        />

        <button className="icon-btn" onClick={handleStar} title="Star">
          <StarIcon />
        </button>

        <button className="icon-btn" onClick={handleImportant} title="Important">
          <ImportantIcon />
        </button>
      </div>

      <div className="email-summary" onClick={handleRead}>
        <span className="sender">{email.from}</span>
        <span className="subject">{email.subject}</span>
        <span className="date">{new Date(email.timestamp).toLocaleString()}</span>
      </div>

      <div className="email-item-right">
        <button className="icon-btn" onClick={handleRead} title="Toggle Read">
          <ReadIcon />
        </button>

        <button className="icon-btn" onClick={handleSnooze} title="Snooze">
          <SnoozeIcon />
        </button>

        <button className="icon-btn" onClick={() => onDelete(email.id)} title="Delete">
          <Trash />
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