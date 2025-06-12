import { useState, useEffect } from "react";
import { createEmail, updateEmail } from "../services/mailService";

export default function CreateMail({ onSend }) {

    const [draft, setDraft] = useState(null);
    const [to, setTo] = useState('');
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Create a new draft when the component mounts
    useEffect(() => {
        const createDraft = async () => {
           try {
                const data = await createEmail({ to: "", subject: "", body: "", send: false });
                console.log("Created draft:", data); // ודאי ש־data.id קיים
                setDraft(data);
            } catch (err) {
                setError(err.message);
            }
        };
        createDraft();
    }, []);

    // Save changes to the draft
    useEffect(() => {
        if (!draft || !draft.id) {
            return;
        }
        const timeout = setTimeout(() => {
            console.log("Auto-saving draft:", draft.id)
            updateEmail(draft.id, { to, subject, body })
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
        console.log(draft);
        console.log(draft?.id);

         console.log("Sending..."); // ← האם זה מופיע בקונסול?
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
            onSend && onSend(sentMail); // callback לשימוש חיצוני
        } catch (err) {
            setError(err.message);
        }
    };


    return (
        <div className="p-4 rounded shadow-md bg-white w-full max-w-md mx-auto">
            <h2 className="text-xl font-bold mb-4"> New Mail</h2>

            <input
                type="email"
                placeholder="to:"
                value={to}
                onChange={(e) => setTo(e.target.value)}
                className="w-full border p-2 mb-2 rounded"
                required
            />

            <input
                type="text"
                placeholder="subject:"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                className="w-full border p-2 mb-2 rounded"
            />

            <textarea
                placeholder=" body:"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                className="w-full border p-2 mb-2 rounded h-32"
            />

            {error && <div className="text-red-600 mb-2">{error}</div>}
            {success && <div className="text-green-600 mb-2">{success}</div>}

            <div className="flex justify-end">
                <button
                    onClick={handleSend}
                    className="px-4 py-2 bg-blue-600 text-white rounded mr-2"
                >
                    Send
                </button>
        </div>
        </div>
    );
}
