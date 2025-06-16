import React, { useEffect, useRef, useState } from 'react';
import { markEmailAsRead } from '../services/mailService';
import '../styles/EmailListToolbar.css';

export default function EmailListToolbar({
  emails = [],
  selectedIds = [],
  onToggleSelectAll,
  onRefresh
}) {
  const checkboxRef = useRef(null);
  const [menuOpen, setMenuOpen]   = useState(false);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState(null);

  const all   = emails.length > 0 && selectedIds.length === emails.length;
  const indet = selectedIds.length > 0 && selectedIds.length < emails.length;

  // set indeterminate state
  useEffect(() => {
    if (checkboxRef.current) {
      checkboxRef.current.indeterminate = indet;
    }
  }, [indet]);

  // close menu when clicking outside
  const menuRef = useRef();
  useEffect(() => {
    const handleClick = e => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  // mark all as read
  const handleMarkAllRead = async () => {
    setError(null);
    setLoading(true);
    try {
      await Promise.all(emails.map(email => markEmailAsRead(email.id)));
      onRefresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
      setMenuOpen(false);
    }
  };

  // refresh action
  const handleRefresh = () => {
    onRefresh();
    setMenuOpen(false);
  };

  return (
    <div className="email-list-toolbar">
      {error && <div className="toolbar-error">{error}</div>}

      <input
        type="checkbox"
        ref={checkboxRef}
        checked={all}
        onChange={onToggleSelectAll}
        className="toolbar-checkbox"
      />

      <button
        onClick={handleRefresh}
        className="toolbar-btn"
        title="Refresh"
        disabled={loading}
      >
        {loading ? 'Refreshing…' : '🔄'}
      </button>

      <div className="toolbar-more" ref={menuRef}>
        <button
          className="toolbar-btn"
          onClick={() => setMenuOpen(o => !o)}
          title="More actions"
          disabled={loading}
        >
          ⋮
        </button>
        {menuOpen && (
          <div className="toolbar-menu">
            <button
              onClick={handleMarkAllRead}
              disabled={loading}
            >
              {loading ? 'Marking…' : 'Mark all as read'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}