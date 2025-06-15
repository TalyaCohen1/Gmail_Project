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
        // Check if draft is created
        if (!draft) {
            setError('Draft not created yet');
            return;
        }

        setError('');
        setSuccess('');
        if (!to || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(to)) {
            setError('Invalid email address');
            return;
        }

        try {
            const sentMail = await updateEmail(draft.id, { to, subject, body, send: true });
            setSuccess('Mail sent successfully!');
            onSend && onSend(sentMail); // callback

            //init all the fields
            setTo('');
            setSubject('');
            setBody('');
            onClose && onClose(); // Close the compose popup
        } catch (err) {
            setError(err.message);
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
