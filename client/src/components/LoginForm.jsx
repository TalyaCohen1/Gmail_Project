import React from "react";

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
        } catch (err) {
            setError(err.message);
        }
    };

    return(
        <form onSubmit= {handleSubmit}>
            <div>
                <label>email</label>
                <input type="email" value={emailAddress}  onChange={e => setEmail(e.target.value)} required/>
            </div>

             <div>
        <label>password:</label>
        <input type="password" value={password}  onChange={e => setPassword(e.target.value)} required />
      </div>
        {error && <div style={{ color: 'red' }}>{error}</div>}

        <button type="submit"> login</button>
        </form> 
    );
}

export default LoginForm;