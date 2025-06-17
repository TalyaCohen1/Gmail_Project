import React, { useState, useEffect, useRef } from 'react';
import {
  deleteEmail,
  addLabelToEmail,
  removeLabelFromEmail,
  markEmailAsRead,
  markEmailAsSpam,
  markEmailAsStarred
} from '../services/mailService';
import { useLabels } from '../context/LabelContext';
import '../styles/BatchActionsBar.css';

export default function BatchActionsBar({ selectedIds = [], onRefresh, onAction }) {
  const { labels, addMailToLabel, deleteMailFromLabel } = useLabels();
  const [labelStates, setLabelStates] = useState({});
  const [labelMenuOpen, setLabelMenuOpen] = useState(false);
  const [activeLabelId, setActiveLabelId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const menuRef = useRef();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setLabelMenuOpen(false);
        setActiveLabelId(null); // סגירת תת־תפריט אחרי לחיצה מחוץ
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [menuRef, onAction]);

  const handleMarkAsRead = async () => {
    setLoading(true);
    try {
      await Promise.all(selectedIds.map(id => markEmailAsRead(id)));
      await onRefresh();
      onAction({ type: 'markAsRead' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleReportSpam = async () => {
    setLoading(true);
    try {
      await Promise.all(selectedIds.map(id => markEmailAsSpam(id)));
      await onRefresh();
      onAction({ type: 'reportSpam' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLabelAction = async (labelId, action) => {
    setLoading(true);
    try {
        if (action === 'add') {
        await Promise.all(selectedIds.map(id => addLabelToEmail(id, labelId)));
        await Promise.all(selectedIds.map(id => addMailToLabel(labelId, id)));
        } else if (action === 'remove') {
        await Promise.all(selectedIds.map(id => removeLabelFromEmail(id, labelId)));
        await Promise.all(selectedIds.map(id => deleteMailFromLabel(labelId, id)));
        }
        await onRefresh();
        onAction({ type: 'labelAction', labelId, action });
        setActiveLabelId(null); // סגירת תת־תפריט אחרי פעולה
    } catch (err) {
        setError(err.message);
    } finally {
        setLoading(false);
    }
  };

  const handleMarkAsStarred = async () => {
    setLoading(true);
    try {
        await Promise.all(selectedIds.map(id => markEmailAsStarred(id)));
        await onRefresh();
        onAction({ type: 'star' });
    } catch (err) {
        setError(err.message);
    } finally {
        setLoading(false);
    }
  };

//   const handleToggleLabel = async (labelId, shouldAdd) => {
//     setLoading(true);
//     try {
//       if (shouldAdd) {
//         await Promise.all(selectedIds.map(id => addLabelToEmail(id, labelId)));
//         await Promise.all(selectedIds.map(id => addMailToLabel(labelId, id)));
//       } else {
//         await Promise.all(selectedIds.map(id => removeLabelFromEmail(id, labelId)));
//         await Promise.all(selectedIds.map(id => deleteMailFromLabel(labelId, id)));
//       }
//       await onRefresh();
//       onAction({ type: 'toggleLabel', labelId });
//     } catch (err) {
//       setError(err.message);
//     } finally {
//       setLoading(false);
//     }
//   };

  const handleDeleteAll = async () => {
    setLoading(true);
    try {
      await Promise.all(selectedIds.map(id => deleteEmail(id)));
      await onRefresh();
      onAction({ type: 'delete' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

//   const handleCheckboxChange = (labelId) => {
//     const newState = !labelStates[labelId];
//     setLabelStates(prev => ({ ...prev, [labelId]: newState }));
//     handleToggleLabel(labelId, newState);
//   };

  return (
    <div className="batch-bar">
      <span>{selectedIds.length} selected</span>

      {/* Mark as Read */}
      <button onClick={handleMarkAsRead} title="Mark as Read">
        <img src={'/icons/mark_as_read.svg'} alt={"Mark as Read"} className="inline-icon" />
      </button>

      {/* Report Spam */}
      <button onClick={handleReportSpam} title="Report Spam">
        <img src={'/icons/report_spam.svg'} alt={"Report Spam"} className="inline-icon" />
      </button>

      {/* Labels with checkboxes */}
      <div className="label-dropdown">
        <button title="Label As" onClick={() => setLabelMenuOpen(prev => !prev)}>
            <img src={'/icons/labels.svg'} alt={"Label As"} className="inline-icon" />
        </button>

        {labelMenuOpen && (
            <div className="label-menu" ref={menuRef}>
            {labels.map(label => (
                <div key={label.id} className="label-item">
                <button
                    className="label-name-btn"
                    onClick={() =>
                    setActiveLabelId(activeLabelId === label.id ? null : label.id)
                    }
                >
                    {label.name}
                </button>

                {activeLabelId === label.id && (
                    <div className="label-submenu">
                    <button onClick={() => handleLabelAction(label.id, 'add')}>
                        Add to label
                    </button>
                    <button onClick={() => handleLabelAction(label.id, 'remove')}>
                        Remove from label
                    </button>
                    </div>
                )}
                </div>
            ))}
          </div>
        )}
      </div>


      {/* Delete */}
      <button onClick={handleDeleteAll} title="Delete">
        <img src={'/icons/trash.svg'} alt={"Delete"} className="inline-icon" />
      </button>
    </div>
  );
}
