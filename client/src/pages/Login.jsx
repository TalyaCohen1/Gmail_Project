// Login.jsx
import React from "react";
import LoginForm
 from "../components/LoginForm";
//screen to display the registration form
const Login = () => {
    const handleLoginSuccess = (token) => {
    console.log("Login successful! Token:", token);
    // Here you can handle the token, e.g., save it to localStorage or context
    };
    return (
        <div style={{ padding: "20px" }}>
            <h1>Login</h1>
            <LoginForm onLoginSuccess={handleLoginSuccess} />
        </div>
    );
};

export default Login;