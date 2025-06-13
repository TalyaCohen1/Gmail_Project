import React from "react";
import {useNavigate, useLocation} from "react-router-dom";
import RegistrationForm from "../components/RegistrationForm";
import "../styles/AuthForm.css";

//screen to display the registration form
const Register = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const handleRegistrationSuccess = () => {
        // After successful registration, navigate to the inbox page
        const from = new URLSearchParams(location.search).get("from") || "/inbox";
        navigate(from);
    };
    return (
    <div className="auth-form-container">
        <div style={{ padding: "20px" }}>
            <h1>User Registration</h1>
                  <RegistrationForm handleRegistrationSuccess = {handleRegistrationSuccess}/>
 
        </div>
    </div>
    );
};

export default Register;