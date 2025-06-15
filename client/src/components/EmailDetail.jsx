// src/components/EmailDetail.jsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getEmailById } from '../services/mailService';
import '../styles/EmailDetail.css';

export default function EmailDetail({ email: inlineEmail, onClose }) {
    const { emailId } = useParams();
    const navigate = useNavigate();
    const [email, setEmail] = useState(inlineEmail || null);

    const isRouteMode = !!emailId;


    useEffect(() => {
        if (isRouteMode) {
            async function fetchData() {
                try {
                    const result = await getEmailById(emailId);
                    setEmail(result);
                } catch (err) {
                    console.error("Failed to fetch email:", err);
                }
            }

            fetchData();
        }
    }, [emailId, isRouteMode]);

    if (!email) return <div className="email-detail loading">Loading...</div>;

    return (
        <div className="email-detail">
            <div className="email-detail-header">
                {isRouteMode ? (
                    <button onClick={() => navigate(-1)}>← Back</button>
                ) : (
                    <button onClick={onClose}>× Close</button>
                )}
                <h2 className="email-subject">{email.subject}</h2>
            </div>

            <div className="email-meta">
                <p><strong>From:</strong> {email.from}</p>
                <p><strong>To:</strong> {email.to}</p>
                <p><strong>Date:</strong> {new Date(email.date).toLocaleString()}</p>
            </div>

            <div className="labels">
                {(email.labels || []).map(label => (
                    <span key={label.id} className="tag">{label.name}</span>
                ))}
            </div>

            <hr />

            <div className="email-body">
                {email.body}
            </div>

            <hr />

            <div className="email-actions">
                <button>★ Star</button>
                <button>↩️ Reply</button>
                <button>↪️ Forward</button>
            </div>
        </div>
    );
}