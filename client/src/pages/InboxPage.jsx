import React, { useState, useEffect, useContext } from 'react';
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList";
import BatchActionsBar from '../components/BatchActionsBar';
import { getInboxEmails, deleteEmail } from '../services/mailService';
import { useDisplayEmails } from '../context/DisplayEmailsContext'; // NEW: Import DisplayEmailsContext hook
import "../styles/InboxPage.css";
import { LabelContext } from '../context/LabelContext';

export default function InboxPage({ isSidebarOpen, toggleSidebar }) {
    // Use the new context for displayed emails and their loading/error states
    const { displayedEmails, setDisplayedEmails, displayLoading, setDisplayLoading, displayError, setDisplayError } = useDisplayEmails();
    const [selectedIds, setSelectedIds] = useState([]);
    const { labels } = useContext(LabelContext);
    // This useEffect will now use the context's setters to update displayed emails
    useEffect(() => {
        const fetchData = async () => {
            setDisplayLoading(true); // Set loading state from context
            setDisplayError(null);   // Clear any previous errors
            try {
                const newEmails = await getInboxEmails();
                setDisplayedEmails(newEmails); // Update the displayed emails using the context setter
            } catch (err) {
                setDisplayError(err.message); // Set error state from context
            } finally {
                setDisplayLoading(false); // Set loading state from context
            }
        };

        fetchData();
        const interval = setInterval(fetchData, 3000);
        return () => clearInterval(interval);
    }, [setDisplayedEmails, setDisplayLoading, setDisplayError]); // Depend on setters from context

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
    
    const performBatchAction = ({ type, labelId }) => {
      if (type === 'delete') {
        selectedIds.forEach(id => handleDelete(id));
      } else if (type === 'addLabel') {
        selectedIds.forEach(id =>
          updateEmail(id, { labels: [...(emails.find(e=>e.id===id).labels||[]), labelId] })
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
            {/* ← כאן שמים את ה־BatchActionsBar */}
            {selectedIds.length > 0 && (
                <BatchActionsBar
                  selectedCount={selectedIds.length}
                  labels={labels}
                  onAction={performBatchAction}
                />
            )}
            <EmailList
                            emails={displayedEmails} // Render emails from context
                            selectedIds={selectedIds}
                            onToggleSelect={toggleSelect}
                            onDelete={handleDelete}
                        />
                    </>
          )}
                </div>
            </div>
        </div>
    );
}