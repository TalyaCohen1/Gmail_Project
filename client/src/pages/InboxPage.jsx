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
    setDisplayedEmails(prev => prev.filter(e => e.id !== id));
    setSelectedIds(prev => prev.filter(x => x !== id));
  };

  const handleMarkAllRead = async () => {
    await Promise.all(displayedEmails.map(m => markEmailAsRead(m.id)));
    await fetchData();
  };

  const performBatchAction = ({ type, labelId }) => {
    if (type === 'delete') {
      selectedIds.forEach(id => handleDelete(id));
    } else if (type === 'addLabel') {
      selectedIds.forEach(id => {
        const current = emails.find(e => e.id === id);
        addLabelToEmail(id, { labels: [...(current.labels || []), labelId] });
      });
    }
    setSelectedIds([]);
  };

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

              {openedEmail ? (
                <EmailDetail
                  email={openedEmail}
                  onClose={() => setOpenedEmail(null)}
                />
              ) : (
                <EmailList
                  emails={displayedEmails}
                  selectedIds={selectedIds}
                  onToggleSelect={toggleSelect}
                  onDelete={handleDelete}
                  onOpenEmail={setOpenedEmail}
                  onToggleStar={(id, v) => v ? markEmailAsStarred(id) : unmarkEmailAsStarred(id)}
                  onToggleImportant={(id, isImportant) => isImportant ? markEmailAsImportant(id) : unmarkEmailAsImportant(id)}
                  onMarkRead={id => markEmailAsRead(id)}
              />
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
