// BlacklistForm.jsx
import React, { useState } from 'react';
import '../styles/BlacklistForm.css';
import { useNavigate } from 'react-router-dom';

const BlacklistForm = () => {
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!email.trim()) {
            setError('Email is required');
            return;
        }

        try {
            const response = await fetch('http://localhost:3000/blacklist', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify({ email })
            });

            if (!response.ok) {
                throw new Error('Failed to add email to blacklist');
            }

            alert('Email added to blacklist successfully');
            navigate('/inbox'); // Redirect to inbox after successful submission
        } catch (err) {
            console.error(err);
            setError('An error occurred while adding the email to the blacklist');
        }
    };

    return (
        <div className="blacklist-form-container">
            <h2>Add Email to Blacklist</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter email address"
                    required
                />
                {error && <p className="error-message">{error}</p>}
                <button type="submit">Add to Blacklist</button>
            </form>
        </div>
    );
}
export default BlacklistForm;