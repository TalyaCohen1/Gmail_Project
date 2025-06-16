import React, { useState, useEffect, useContext } from 'react';
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList";
import BatchActionsBar from '../components/BatchActionsBar';
import EmailListToolbar from "../components/EmailListToolbar";
import { getInboxEmails, deleteEmail, addLabelToEmail, removeLabelFromEmail, markEmailAsRead, markEmailAsUnread } from '../services/mailService';
import { useDisplayEmails } from '../context/DisplayEmailsContext'; // NEW: Import DisplayEmailsContext hook
import "../styles/InboxPage.css";
import { LabelContext } from '../context/LabelContext';

export default function InboxPage({ isSidebarOpen, toggleSidebar }) {
  // Use the new context for displayed emails and their loading/error states
  const { displayedEmails, setDisplayedEmails, displayLoading, setDisplayLoading, displayError, setDisplayError } = useDisplayEmails();
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const [emails, setEmails] = useState([]);
  const { labels } = useContext(LabelContext);
  
  const fetchData = async () => {
    setLoading(true);
    try {
      const newEmails = await getInboxEmails();
      setError(null);
      setEmails(prev => {
        const same =
          prev.length === newEmails.length &&
          prev.every((e, i) => e.id === newEmails[i].id);
        return same ? prev : newEmails;
      });
    } catch (err) {
        setError(err.message);
    } finally {
        setLoading(false);
    }
};

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  const toggleSelect = id =>
      setSelectedIds(prev =>
          prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
      );

  const handleDelete = async id => {
      await deleteEmail(id);
      // Update displayed emails after deletion by filtering out the deleted email
      setDisplayedEmails(prev => prev.filter(e => e.id !== id));
      // Also remove from selectedIds if it was selected
      setSelectedIds(prev => prev.filter(x => x !== id));
  };

  const handleMarkAllRead = async () => {
    await Promise.all(
      displayedEmails.map(m =>
        markEmailAsRead(m.id)
      )
    );
    await fetchData();
  };

  const performBatchAction = ({ type, labelId }) => {
    if (type === 'delete') {
      selectedIds.forEach(id => handleDelete(id));
    } else if (type === 'addLabel') {
      selectedIds.forEach(id =>
        addLabelToEmail(id, { labels: [...(emails.find(e => e.id === id).labels || []), labelId] })
      );
    }
    setSelectedIds([]);
  };

  return (
    <div className="inbox-page">
      <Header toggleSidebar={toggleSidebar} />
      <div className="main-content-area">
        {/* Pass the setters for displayed emails to Sidebar */}
        <Sidebar
          isSidebarOpen={isSidebarOpen}
          setDisplayedEmails={setDisplayedEmails} // Passed down as prop
          setDisplayLoading={setDisplayLoading}   // Passed down as prop
          setDisplayError={setDisplayError}       // Passed down as prop
        />
          <div className="email-list-container">
            {displayLoading && <p>Loading emails…</p>} {/* Use context's loading state */}
            {displayError   && <p style={{ color: 'red' }}>{displayError}</p>} {/* Use context's error state */}
            {!displayLoading && !displayError && (
              <>                
              {selectedIds.length > 0 ? (
              <BatchActionsBar
                selectedIds={selectedIds}
                onAction={performBatchAction}
                labels={labels}
                onRefresh={fetchData}
              />
              ) : (
                <EmailListToolbar
                emails={displayedEmails}
                selectedIds={selectedIds}
                onToggleSelectAll={() => {
                  if (selectedIds.length === displayedEmails.length) {
                    setSelectedIds([]);
                  } else {
                    setSelectedIds(displayedEmails.map(e => e.id));
                  }
                }}
                onRefresh={fetchData}
                onMarkAllRead={handleMarkAllRead}
              />
            )}
              <EmailList
                emails={displayedEmails} // Render emails from context
                selectedIds={selectedIds}
                onToggleSelect={toggleSelect}
                onDelete={handleDelete}
                onToggleStar={(id, v) => updateEmail(id, { starred: v })}
                onToggleImportant={(id, v) => updateEmail(id, { important: v })}
                onMarkRead={id => updateEmail(id, { read: true })}
                onSnooze={id => snoozeEmail(id)}
              />
            </>
          )}
          </div>
      </div>
    </div>
  );
}