// Login.jsx
import React from "react";
import {useNavigate} from "react-router-dom";
import LoginForm from "../components/LoginForm";
//screen to display the registration form
const Login = () => {
    const handleLoginSuccess = (token) => {
    localStorage.setItem("token", token); // Save the token to localStorage
    console.log("Login successful! Token:", token);
    // navigate("inbox"); // Redirect to the inbox page
    };
    return (
        <div style={{ padding: "20px" }}>
            <h1>Login</h1>
            <LoginForm onLoginSuccess={handleLoginSuccess} />
        </div>
    );
};

export default Login;