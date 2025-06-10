// Inbox.jsx

import React, { useEffect, useState, useContext } from 'react';
import { getEmails, deleteEmail, updateEmail } from '../services/mailService';
//import { LabelsContext } from '../context/LabelsContext';
import LabelManager from '../components/LabelManager.jsx'; 
import EmailList from '../components/EmailList.jsx';
import BatchActionsBar from '../components/BatchActionsBar.jsx';
import '../styles/InboxPage.css';

export default function InboxPage() {
    const [emails, setEmails] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedIds, setSelectedIds] = useState([]);
    /* const { labels } = useContext(LabelsContext); */

    useEffect(() => {
    getEmails()
        .then(data => setEmails(data))
        .catch(err => setError(err.message))
        .finally(() => setLoading(false));
    }, []);

    const toggleSelect = id => {
        setSelectedIds(prev =>
        prev.includes(id)
            ? prev.filter(x => x !== id)
            : [...prev, id]
        );
    };

    const handleDelete = async id => {
        await deleteEmail(id);
        setEmails(emails.filter(e => e.id !== id));
        setSelectedIds(ids => ids.filter(x => x !== id));
    };

    
    // batch update/delete passed into BatchActionsBar
    const performBatchAction = ({ type, labelId }) => {
        if (type === 'delete') {
            selectedIds.forEach(handleDelete);
        } else if (type === 'addLabel') {
            selectedIds.forEach(id => {
                const email = emails.find(e => e.id === id);
                /*const newLabels = [...email.labels, labelId]; */
                /*updateEmail(id, { labels: newLabels }).then(updated => {
                    setEmails(emails.map(e => e.id === id ? updated : e));
                });*/
            });
        setSelectedIds([]);
        }
    };


  if (loading) return <div>Loading emails...</div>;
  if (error) return <div>Error: {error}</div>;

  return (

        <div className="inbox-page">
        {selectedIds.length > 0 && (
            <BatchActionsBar
            selectedCount={selectedIds.length}
            /*labels={labels}*/
            onAction={performBatchAction}
            />
        )}
        <EmailList
            emails={emails}
            selectedIds={selectedIds}
            onToggleSelect={toggleSelect}
            onDelete={handleDelete}
        />
        </div>
    );
}

