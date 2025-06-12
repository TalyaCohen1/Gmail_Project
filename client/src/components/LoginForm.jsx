//src/components/LoginForm.jsx
import React from "react";
import { Link } from "react-router-dom";

function LoginForm({onLoginSuccess }) {
    const [emailAddress, setEmail] = React.useState('');
    const [password, setPassword] = React.useState('');
    const [error, setError] = React.useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!emailAddress || !password) {
            setError('Email and password are required');
            return;
        }

        try {
            const response = await fetch('http://localhost:3000/api/tokens', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ emailAddress, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Login failed');
            }

            const data = await response.json();
            onLoginSuccess(data.token); // Assuming the response contains a token
            localStorage.setItem('fullName', data.fullName);
                  if (data.profileImage) {
                    localStorage.setItem('profileImage', data.profileImage);
                  } else {
                    localStorage.setItem('profileImage', '/uploads/default-profile.png');
                  } 
           } catch (err) {
            setError(err.message);
        }
    };

    return (
    <form onSubmit={handleSubmit} className="auth-container">
      <div className="google-logo">
        <h1>Sign in to MyMail</h1>
      </div>

      <p className="auth-subtitle">Use your MyMail account</p>

      <div className="form-group">
        <label htmlFor="email">Email address</label>
        <input
          id="email"
          type="email"
          value={emailAddress}
          onChange={(e) => setEmail(e.target.value)}
          className={error ? "error" : ""}
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className={error ? "error" : ""}
          required
        />
      </div>

      {error && <div className="general-error">{error}</div>}

      <button type="submit" className="auth-button">Login</button>

      <div className="form-footer">
        <Link to="/register">Create account</Link>
        </div>
    </form>
  );
}

export default LoginForm;