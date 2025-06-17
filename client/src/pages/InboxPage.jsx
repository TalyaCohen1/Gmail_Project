import React, { useState, useEffect, useContext } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList";
import EmailDetail from '../components/EmailDetail';
import BatchActionsBar from '../components/BatchActionsBar';
import EmailListToolbar from "../components/EmailListToolbar";
import {
  getInboxEmails,
  deleteEmail,
  markEmailAsRead,
  markEmailAsUnread,
  markEmailAsImportant,
  markEmailAsStarred,
  unmarkEmailAsImportant,
  unmarkEmailAsStarred,
  addLabelToEmail,
  removeLabelFromEmail
} from '../services/mailService';
import { useDisplayEmails } from '../context/DisplayEmailsContext';
import { LabelContext } from '../context/LabelContext';
import "../styles/InboxPage.css";

// Helper function to sort emails by date (newest first)
const sortEmailsByDate = (emails) => {
  return [...emails].sort((a, b) => {
    const dateA = new Date(a.timestamp || a.date);
    const dateB = new Date(b.timestamp || b.date);
    return dateB - dateA; // Newest first (descending order)
  });
};

export default function InboxPage({ isSidebarOpen, toggleSidebar }) {
  const {
    displayedEmails,
    setDisplayedEmails,
    displayLoading,
    setDisplayLoading,
    displayError,
    setDisplayError
  } = useDisplayEmails();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const [openedEmail, setOpenedEmail] = useState(null);
  const [emails, setEmails] = useState([]);

  const { labels } = useContext(LabelContext);

  const fetchData = async () => {
    setLoading(true);
    try {
      const newEmails = await getInboxEmails();
      setError(null);
      const sortedEmails = sortEmailsByDate(newEmails);
      setEmails(prev => {
        const same =
          prev.length === sortedEmails.length &&
          prev.every((e, i) => e.id === sortedEmails[i].id);
        return same ? prev : sortedEmails;
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

  // Helper function to update email in both states
  const updateEmailInStates = (emailId, updates) => {
    const updateFn = prevEmails => 
      prevEmails.map(email => 
        email.id === emailId 
          ? { ...email, ...updates }
          : email
      );
    
    setEmails(updateFn);
    setDisplayedEmails(updateFn);
  };

  const toggleSelect = id =>
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );

  const handleDelete = async id => {
    try {
      await deleteEmail(id);
      setDisplayedEmails(prev => prev.filter(e => e.id !== id));
      // setEmails(prev => prev.filter(e => e.id !== id));
      setSelectedIds(prev => prev.filter(x => x !== id));
    } catch (error) {
      console.error('Error deleting email:', error);
    }
  };

  const handleToggleStar = async (id, isStarred) => {
    try {
      // Update local state immediately for instant UI feedback
      updateEmailInStates(id, { isStarred: isStarred });
      
      // Then call the API
      if (isStarred) {
        await markEmailAsStarred(id);
      } else {
        await unmarkEmailAsStarred(id);
      }
    } catch (error) {
      console.error('Error toggling star:', error);
      // Revert on error
      updateEmailInStates(id, { isStarred: !isStarred });
    }
  };

  const handleToggleImportant = async (id, isImportant) => {
    try {
      // Update local state immediately for instant UI feedback
      updateEmailInStates(id, { isImportant: isImportant });

      // Then call the API
      if (isImportant) {
        await markEmailAsImportant(id);
      } else {
        await unmarkEmailAsImportant(id);
      }
    } catch (error) {
      console.error('Error toggling important:', error);
      // Revert on error
      updateEmailInStates(id, { isImportant: !isImportant });
    }
  };

  const handleToggleRead = async (id, isRead) => {
    try {
      // Update local state immediately for instant UI feedback
      updateEmailInStates(id, { isRead: isRead });

      // Then call the API
      if (isRead) {
        await markEmailAsRead(id);
      } else {
        await markEmailAsUnread(id);
      }
    } catch (error) {
      console.error('Error toggling read status:', error);
      // Revert on error
      updateEmailInStates(id, { isRead: !isRead });
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await Promise.all(displayedEmails.map(m => markEmailAsRead(m.id)));
      // Update all displayed emails to read status
      const updateFn = prevEmails =>
        prevEmails.map(email => ({ ...email, isRead: true }));
      setEmails(updateFn);
      setDisplayedEmails(updateFn);
    } catch (error) {
      console.error('Error marking all as read:', error);
      await fetchData(); // Refresh on error
    }
  };

  const performBatchAction = async ({ type, labelId }) => {
    try {
      if (type === 'delete') {
        await Promise.all(selectedIds.map(id => deleteEmail(id)));
        setDisplayedEmails(prev => prev.filter(e => !selectedIds.includes(e.id)));
        setEmails(prev => prev.filter(e => !selectedIds.includes(e.id)));
      } else if (type === 'addLabel') {
        await Promise.all(selectedIds.map(id => {
          const current = emails.find(e => e.id === id);
          return addLabelToEmail(id, { labels: [...(current.labels || []), labelId] });
        }));
        // Update local state with new labels
        const updateFn = prevEmails => 
          prevEmails.map(email => 
            selectedIds.includes(email.id)
              ? { ...email, labels: [...(email.labels || []), labelId] }
              : email
          );
        setEmails(updateFn);
        setDisplayedEmails(updateFn);
      } else if (type === 'clearSelection') {
        setSelectedIds([]);
      }
      setSelectedIds([]);
    } catch (error) {
      console.error('Error performing batch action:', error);
      await fetchData(); // Refresh on error
    }
  };

  // Sort displayed emails for rendering
  const sortedDisplayedEmails = sortEmailsByDate(displayedEmails);

  return (
    <div className="inbox-page">
      <Header toggleSidebar={toggleSidebar} />

      <div className="main-content-area">
        <Sidebar
          isSidebarOpen={isSidebarOpen}
          setDisplayedEmails={setDisplayedEmails}
          setDisplayLoading={setDisplayLoading}
          setDisplayError={setDisplayError}
        />

        <div className="email-list-container">
          {displayLoading && <p>Loading emails…</p>}
          {displayError && <p style={{ color: 'red' }}>{displayError}</p>}

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
                  emails={sortedDisplayedEmails}
                  selectedIds={selectedIds}
                  onToggleSelectAll={() => {
                    if (selectedIds.length === sortedDisplayedEmails.length) {
                      setSelectedIds([]);
                    } else {
                      setSelectedIds(sortedDisplayedEmails.map(e => e.id));
                    }
                  }}
                  onRefresh={fetchData}
                  onMarkAllRead={handleMarkAllRead}
                />
              )}

              {openedEmail ? (
                <EmailDetail
                  email={openedEmail}
                  onClose={() => setOpenedEmail(null)}
                />
              ) : (
                <EmailList
                  emails={sortedDisplayedEmails}
                  selectedIds={selectedIds}
                  onToggleSelect={toggleSelect}
                  onDelete={handleDelete}
                  onOpenEmail={setOpenedEmail}
                  onToggleStar={handleToggleStar}
                  onToggleImportant={handleToggleImportant}
                  onToggleRead={handleToggleRead}
                />
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}