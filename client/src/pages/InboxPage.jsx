import React, { useState, useEffect } from 'react';
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList"
import { getInboxEmails, deleteEmail, updateEmail } from '../services/mailService';
import "../styles/InboxPage.css";

export default function InboxPage({ isSidebarOpen, toggleSidebar }) {
    const [emails, setEmails] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedIds, setSelectedIds] = useState([]);


  useEffect(() => {
      const fetchData = async () => {
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
      setEmails(emails.filter(e => e.id !== id));
    };
    
    return (
    <div className="inbox-page">
      <Header toggleSidebar={toggleSidebar} />
      <div className="main-content-area">
        <Sidebar isSidebarOpen={isSidebarOpen} />
        <div className="email-list-container">
          {loading && <p>Loading emails…</p>}
          {error   && <p style={{ color: 'red' }}>{error}</p>}
          {!loading && !error && (
            <EmailList
              emails={emails}
              selectedIds={selectedIds}
              onToggleSelect={toggleSelect}
              onDelete={handleDelete}
            />
          )}
        </div>
      </div>
    </div>
  );
}
