import React, { useState, useEffect, useContext, useRef, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import { Routes, Route } from 'react-router-dom';
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList";
import EmailDetail from '../components/EmailDetail';
import BatchActionsBar from '../components/BatchActionsBar';
import EmailListToolbar from "../components/EmailListToolbar";
import {
  getInboxEmails,
  getSentEmails,
  getDraftEmails,
  getSpamEmails,
  getDeletedEmails,
  getImportantEmails,
  getStarredEmails,
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
import { LabelContext, useLabels } from '../context/LabelContext';
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
  const [currentView, setCurrentViewState] = useState(() => {
    //return sessionStorage.getItem('currentView') || 'inbox';
    const storedView = sessionStorage.getItem('currentView');
    return storedView || 'inbox';
  });

  const setCurrentView = (view) => {
    sessionStorage.setItem('currentView', view);
    setCurrentViewState(view);
  };

  const { labels } = useContext(LabelContext);
  const { fetchMailsForLabel } = useLabels();

  const sidebarRef = useRef();
  const location = useLocation();

const refreshAll = useCallback(async () => {
  setDisplayLoading(true);
  try {
    if (!currentView) {
      setCurrentView('inbox');
      return;
    }
    // Refresh sidebar counts
    if (sidebarRef.current && sidebarRef.current.refreshCounts) {
      await sidebarRef.current.refreshCounts();
    }

    // Refresh the currently displayed view
    let emails = [];
    const lowerCaseCurrentView = currentView.toLowerCase();
    const foundLabel = labels.find(l => l.name.toLowerCase() === lowerCaseCurrentView);
    if (foundLabel) {
        emails = await fetchMailsForLabel(foundLabel.id);
    } else {
      switch (currentView) {
        case 'sent':
          emails = await getSentEmails(); break;
        case 'drafts':
          emails = await getDraftEmails(); break;
        case 'spam':
          emails = await getSpamEmails(); break;
        case 'deleted':
          emails = await getDeletedEmails(); break;
        case 'important':
          emails = await getImportantEmails(); break;
        case 'starred':
          emails = await getStarredEmails(); break;
        case 'social':
        case 'updates':
        case 'forums':
        case 'promotions':
          const label = labels.find(l => l.name.toLowerCase() === currentView);
          emails = label ? await fetchMailsForLabel(label.id) : [];
          break;
        default:
          emails = await getInboxEmails(); break;
      }
    }

    setDisplayedEmails(sortEmailsByDate(emails));
    setDisplayError(null);
  } catch (err) {
    setDisplayError(err.message || 'Failed to refresh');
  } finally {
    setDisplayLoading(false);
  }
}, [
    currentView, labels, fetchMailsForLabel, getSentEmails, getDraftEmails,
    getSpamEmails, getDeletedEmails, getImportantEmails, getStarredEmails,
    getInboxEmails, sortEmailsByDate, setDisplayedEmails, setDisplayError, setDisplayLoading
]);

  const refreshAllIntervalRef = useRef(refreshAll);

  useEffect(() => {
    refreshAllIntervalRef.current = refreshAll;
  }, [refreshAll]);

  useEffect(() => {
    refreshAllIntervalRef.current();
    const interval = setInterval(() => {
        refreshAllIntervalRef.current();
    }, 10000); 

    return () => {
        clearInterval(interval);
    };
}, []);
  
  // useEffect(() => {
  //   refreshAll();
  //   // const interval = setInterval(refreshAll, 3000);
  //   const interval = setInterval(() => {
  //       refreshAll(); 
  //   }, 10000);
  // //   return () => clearInterval(interval);
  // // }, []);
  // return () => {
  //       clearInterval(interval);
  //   };
  // }, [currentView]);

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
      await refreshAll(); // Refresh on error
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
      await refreshAll(); // Refresh on error
    }
  };

  // Sort displayed emails for rendering
  const sortedDisplayedEmails = sortEmailsByDate(displayedEmails);

  return (
    <div className="inbox-page">
      <Header toggleSidebar={toggleSidebar} />

      <div className="main-content-area">
        <Sidebar
          ref={sidebarRef}
          isSidebarOpen={isSidebarOpen}
          setDisplayedEmails={setDisplayedEmails}
          setDisplayLoading={setDisplayLoading}
          setDisplayError={setDisplayError}
          setCurrentView={setCurrentView}
          currentView={currentView}
        />

        <div className="email-list-container">
          {displayError && <p style={{ color: 'red' }}>{displayError}</p>}
            <>
              {selectedIds.length > 0 ? (
                <BatchActionsBar
                  selectedIds={selectedIds}
                  onAction={performBatchAction}
                  labels={labels}
                  onRefresh={refreshAll}
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
                  onRefresh={refreshAll}
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
        </div>
      </div>
    </div>
  );
}