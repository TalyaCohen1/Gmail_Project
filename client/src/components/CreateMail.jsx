import { useState, useEffect, useRef } from "react";
import { createEmail, updateEmail } from "../services/mailService";
import "../styles/Mail.css";

export default function CreateMail({ onSend, onClose }) {

    const [draft, setDraft] = useState(null);
    const [to, setTo] = useState('');
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [isMinimized, setIsMinimized] = useState(false);
    const [isMaximized, setIsMaximized] = useState(false);
    const hasCreatedDraft = useRef(false);


    // Function to toggle the minimized state
    const handleMinimize = () => {
        setIsMinimized(!isMinimized);
        if (!isMinimized) setIsMaximized(false);
    };

    // Function to toggle the maximized state
    const handleMaximize = () => {
        setIsMaximized(!isMaximized);
        if (!isMaximized) setIsMinimized(false);
    };

    // Initialize a new draft
    const createDraft = async () => {
           try {
                const data = await createEmail({ to: " ", subject: " ", body: " ", send: false });
                setDraft(data);
            } catch (err) {
                setError(err.message);
            }
        };

    // Create a new draft when the component mounts
    useEffect(() => {
    if (!hasCreatedDraft.current) {
        createDraft();
        hasCreatedDraft.current = true;
    }
}, []);
        

    // Save changes to the draft
    useEffect(() => {
        if (!draft || !draft.id) {
            return;
        }
        setIsSaving(true);
        const timeout = setTimeout(() => {
            updateEmail(draft.id, { to, subject, body, send :false })
                .then(updated => setDraft(updated))
                .catch(err => {
                console.error("Failed to auto-save draft", err);
                setError(err.message);
            });
        }, 1000);

        return () => clearTimeout(timeout);
    }, [to, subject, body]);


    // Send the mail
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

            // אם זה לא הראשון, צור טיוטה חדשה
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
            onSend && onSend(sentMail); // ✅ הוספת callback עבור כל שליחה
        }

        setSuccess('Mail sent successfully!');
        setTo('');
        setSubject('');
        setBody('');
        onClose && onClose();
    } catch (err) {
        if (err.message.includes('Recipient email')) {
            setError('One or more email addresses are not registered in the system.');
        } else {
            setError(err.message);
        }
    }
};


    const handleNewMail = async () => {
        setTo('');
        setSubject('');
        setBody('');
        setError('');
        setSuccess('');
        await createDraft();
    };


    return (
        <div className={`compose-popup ${isMinimized ? 'minimized' : ''} ${isMaximized ? 'maximized' : ''}`}>
            <div className="header">
                <h2>New Message</h2>
                <div className="window-controls">
                    <button onClick={handleMinimize}>_</button>
                    <button onClick={handleMaximize}>□</button>
                    <button onClick={onClose}>✕</button>
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

            <div className="actions">
                <button onClick={handleSend}>Send</button>
            </div>

            {isSaving && <div style={{ color: '#888', fontSize: '12px', marginTop: '8px' }}>Saving...</div>}
            {!isSaving && draft && <div style={{ color: 'green', fontSize: '12px', marginTop: '8px' }}>Saved</div>}
        </div>
    );
}
