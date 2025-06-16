// src/components/EmailActions.jsx
import React, { useState } from 'react';
import CreateMail from './CreateMail';

export default function EmailActions({ email, onEmailUpdate }) {
    const [action, setAction] = useState(null); // "reply", "forward", or "edit"

    const isDraft = () => {
        if (email.send === false || email.send === 'false') return true;
        if (email.labels?.some(label =>
            label.name?.toLowerCase() === 'drafts'
        )) return true;
        return false;
    };

    const isEmailDraft = isDraft();

    const getQuotedBody = (label) => {
        return `\n\n--- ${label} ---\nFrom: ${email.from}\nDate: ${new Date(email.date).toLocaleString()}\nSubject: ${email.subject}\n\n${email.body}`;
    };

    const getDefaultValues = () => {
        if (action === 'reply') {
            return {
                to: email.from,
                subject: `RE: ${email.subject}`,
                body: getQuotedBody('Original Message')
            };
        }
        if (action === 'forward') {
            return {
                to: '',
                subject: `FWD: ${email.subject}`,
                body: getQuotedBody('Forwarded Message')
            };
        }
        return null;
    };

    const handleActionComplete = (sentEmail) => {
        setAction(null);
        if (onEmailUpdate && sentEmail) {
            onEmailUpdate(sentEmail);
        }
    };

    const shouldShowComposer = action === 'reply' || action === 'forward' || (action === 'edit' && isEmailDraft);

    return (
        <>
            <div className="email-actions">
                {isEmailDraft ? (
                    <button onClick={() => setAction('edit')}>
                        ✏️ Edit Draft
                    </button>
                ) : (
                    <>
                        <button onClick={() => console.log('Star clicked')}>
                            ★ Star
                        </button>
                        <button onClick={() => setAction('reply')}>
                            ↩️ Reply
                        </button>
                        <button onClick={() => setAction('forward')}>
                            ↪️ Forward
                        </button>
                    </>
                )}
            </div>

            {shouldShowComposer && (
                <div className="inline-reply">
                    <CreateMail
                        existingEmail={action === 'edit' ? email : undefined}
                        defaultValues={getDefaultValues()}
                        onSend={handleActionComplete}
                        onClose={() => setAction(null)}
                    />
                </div>
            )}
        </>
    );
}
