// CreateMail.jsx
import { useState, useEffect, useRef } from "react";
import { createEmail, updateEmail } from "../services/mailService";
import "../styles/Mail.css";

export default function CreateMail({ onSend, onClose, existingEmail = null, defaultValues = null, readOnlyActions = false ,onRefresh }) {
    // State for managing draft and input fields
    const [draft, setDraft] = useState(null);
    const [to, setTo] = useState(() => {
        if (defaultValues?.to) return defaultValues.to;
        if (Array.isArray(existingEmail?.to)) return existingEmail.to.join(', ');
        if (typeof existingEmail?.to === 'string') return existingEmail.to;
        return '';
        });
    const [subject, setSubject] = useState(defaultValues?.subject || existingEmail?.subject || '');
    const [body, setBody] = useState(defaultValues?.body || existingEmail?.body || '');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [isMinimized, setIsMinimized] = useState(false);
    const [isMaximized, setIsMaximized] = useState(false);
    const hasCreatedDraft = useRef(false);

    // Toggle minimize state
    const handleMinimize = () => {
        setIsMinimized(!isMinimized);
        if (!isMinimized) setIsMaximized(false);
    };
    // Toggle maximize state
    const handleMaximize = () => {
        setIsMaximized(!isMaximized);
        if (!isMaximized) setIsMinimized(false);
    };

    // Create a new draft email if no existing email is provided
    const createDraft = async () => {
        try {
            const data = await createEmail({ to: " ", subject: " ", body: " ", send: false });
            setDraft(data);
        } catch (err) {
            setError(err.message);
        }
    };

        // Create or load draft on mount
    useEffect(() => {
        if (existingEmail) {
            setDraft(existingEmail);
        } else if (!hasCreatedDraft.current) {
            (async () => {
                try {
                    const data = await createEmail({ to: " ", subject: " ", body: " ", send: false });
                    setDraft(data);
                    hasCreatedDraft.current = true;
                } catch (err) {
                    setError(err.message);
                }
            })();
        }
    }, [existingEmail]);
    
    // Auto-save draft after 1 second of inactivity on input
    useEffect(() => {
        if (!draft || !draft.id || existingEmail?.id) return;
        setIsSaving(true);
        const timeout = setTimeout(() => {
            updateEmail(draft.id, { to, subject, body, send: false })
                .then(updated => setDraft(updated))
                .catch(err => {
                    console.error("Failed to auto-save draft", err);
                    setError(err.message);
                })
                .finally(() => setIsSaving(false));
        }, 1000);
        return () => clearTimeout(timeout);
    }, [to, subject, body]);

        // Handle email sending (including duplication for multiple recipients)
    const handleSend = async () => {
        if (!draft) {
            setError('Draft not created yet');
            return;
        }
        setError('');
        setSuccess('');

        const recipients = to.split(',').map(email => email.trim()).filter(Boolean);
        if (!recipients.every(email => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))) {
            setError('One or more email addresses are invalid');
            return;
        }

        try {
            const sentMails = [];
            for (let i = 0; i < recipients.length; i++) {
                const recipient = recipients[i];
                let mailIdToSend = draft.id;
                if (i > 0) {
                    const newDraft = await createEmail({ to: " ", subject: " ", body: " ", send: false });
                    mailIdToSend = newDraft.id;
                }
                const sentMail = await updateEmail(mailIdToSend, {
                    to: recipient,
                    subject,
                    body,
                    send: true,
                });
                sentMails.push(sentMail);
                onSend && onSend(sentMail);
            }
            setSuccess('Mail sent successfully!');
            setTo('');
            setSubject('');
            setBody('');
            onRefresh && onRefresh(); // Refresh the email list after sending
            onClose && onClose();
        } catch (err) {
            if (err.message.includes('Recipient email')) {
                setError('One or more email addresses are not registered in the system.');
            } else {
                setError(err.message);
            }
        }
    };
    // Update form values if existingEmail changes
    useEffect(() => {
    if (existingEmail) {
        const safeTo = Array.isArray(existingEmail.to)
            ? existingEmail.to.join(', ')
            : (typeof existingEmail.to === 'string' ? existingEmail.to : '');

        setTo(safeTo);
        setSubject(existingEmail.subject || '');
        setBody(existingEmail.body || '');
    }
}, [existingEmail]);

// Handle closing the popup with saving the draft
    const handleCloseWithSave = async () => {
        if (draft && draft.id) {
            try {
            await updateEmail(draft.id, { to, subject, body, send: false });
            } catch (err) {
            console.error("Failed to save draft on close:", err);
            }
        }
        onClose && onClose();
        onRefresh && onRefresh();
    }
    return (
        <div className={`compose-popup ${isMinimized ? 'minimized' : ''} ${isMaximized ? 'maximized' : ''}`}>
            <div className="header">
                <h2>New Message</h2>
                <div className="window-controls">
                    <button onClick={handleMinimize}>_</button>
                    <button onClick={handleMaximize}>□</button>
                    <button onClick={handleCloseWithSave}>✕</button>
                </div>
            </div>
            <input
                type="email"
                placeholder="To:"
                value={to}
                onChange={(e) => setTo(e.target.value)}
                className="w-full border p-2 mb-2 rounded"
                required
            />
            <input
                type="text"
                placeholder="Subject:"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                className="w-full border p-2 mb-2 rounded"
            />
            <textarea
                placeholder="Message body:"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                className="w-full border p-2 mb-2 rounded h-32"
            />

            {error && <div style={{ color: 'red', marginBottom: '12px' }}>{error}</div>}
            {success && <div style={{ color: 'green', marginBottom: '12px' }}>{success}</div>}

            {!readOnlyActions && (
                <div className="actions">
                    <button onClick={handleSend}>Send</button>
                </div>
            )}

            {isSaving && <div style={{ color: '#888', fontSize: '12px', marginTop: '8px' }}>Saving...</div>}
            {!isSaving && draft && <div style={{ color: 'green', fontSize: '12px', marginTop: '8px' }}>Saved</div>}
        </div>
    );
}
