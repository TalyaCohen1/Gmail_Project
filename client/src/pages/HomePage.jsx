import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/HomePage.css';
// import logo from '../assets/gmailLogo.png';

export default function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <div className="logo-section">
        {/* <img src={logo} alt="Logo" className="logo-img" /> */}
        <h1 className="title">Welcome to SMail</h1>
      </div>

      <div className="auth-buttons">
        <button className="btn-login" onClick={() => navigate('/login')}>Login</button>
        <button className="btn-register" onClick={() => navigate('/register')}>Create account</button>
      </div>
    </div>
  );
}
