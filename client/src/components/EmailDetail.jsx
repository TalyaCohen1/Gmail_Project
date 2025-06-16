// src/components/EmailDetail.jsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getEmailById, markEmailAsRead } from '../services/mailService';
import EmailActions from './EmailActions';
import '../styles/EmailDetail.css';

export default function EmailDetail({ email: inlineEmail, onClose }) {
    const { emailId } = useParams();
    const navigate = useNavigate();
    const [email, setEmail] = useState(inlineEmail || null);
    const [loading, setLoading] = useState(!inlineEmail);
    const [error, setError] = useState('');
    const isRouteMode = !!emailId;

    useEffect(() => {
        if (isRouteMode && !inlineEmail) {
            async function fetchData() {
                try {
                    setLoading(true);
                    const result = await getEmailById(emailId);
                    setEmail(result);
                    setError('');
                } catch (err) {
                    console.error("Failed to fetch email:", err);
                    setError('Failed to load email');
                } finally {
                    setLoading(false);
                }
            }
            fetchData();
        }
    }, [emailId, isRouteMode, inlineEmail]);

    useEffect(() => {
    if (email && email.id) {
        markEmailAsRead(email.id).catch(err =>
            console.error(`Failed to mark email ${email.id} as read:`, err)
        );
    }
    }, [email]);

    if (loading) {
        return <div className="email-detail loading">Loading email...</div>;
    }

    if (error) {
        return (
            <div className="email-detail error">
                <p>Error: {error}</p>
                <button onClick={() => isRouteMode ? navigate(-1) : onClose()}>
                    Go Back
                </button>
            </div>
        );
    }

    if (!email) {
        return <div className="email-detail loading">No email found</div>;
    }

    const handleEmailUpdate = (updatedEmail) => {
        setEmail(updatedEmail);
        if (onClose) onClose();
        if (isRouteMode) {
            navigate(-1);
        }
    };

    return (
        <div className="email-detail">
            <div className="email-detail-header">
                {isRouteMode ? (
                    <button onClick={() => navigate(-1)}>← Back</button>
                ) : (
                    <button onClick={onClose}>× Close</button>
                )}
                <h2 className="email-subject">{email.subject || '(No Subject)'}</h2>
            </div>

            <div className="email-meta">
                <p><strong>From:</strong> {email.from}</p>
                <p><strong>To:</strong> {Array.isArray(email.to) ? email.to.join(', ') : email.to}</p>
                <p><strong>Date:</strong> {new Date(email.date).toLocaleString()}</p>
            </div>

            {email.labels && email.labels.length > 0 && (
                <div className="labels">
                    {email.labels.map(label => (
                        <span key={label.id} className="tag">{label.name}</span>
                    ))}
                </div>
            )}

            <div className="email-body">
                {email.body || '(No content)'}
            </div>

            <EmailActions 
                email={email} 
                onEmailUpdate={handleEmailUpdate}
            />
        </div>
    );
}