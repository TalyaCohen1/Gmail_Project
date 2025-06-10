// Login.jsx
import React from "react";
import {useNavigate, useLocation} from "react-router-dom";
import LoginForm from "../components/LoginForm";
import "../styles/AuthForm.css";

//screen to display the registration form
const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const handleLoginSuccess = (token) => {
    localStorage.setItem("token", token); // Save the token to localStorage
    console.log("Login successful! Token:", token);
    
    //navigate back if we came from a different page
    const from = new URLSearchParams(location.search).get("from") || "/inbox";
    navigate(from);

    };

    const expired = new URLSearchParams(location.search).get("expired") === "1";
    return (
        <div className="auth-form-container">
            <h1>Login</h1>
            {expired == "1" && (
                <div style={{ color: "red" , fontSize: "16px", marginBottom: "20px" }}>
                    Your session has expired. Please log in again.
                </div>
            )}
            <LoginForm onLoginSuccess={handleLoginSuccess} />
        </div>
    );
};

export default Login;