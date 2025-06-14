// App.js

import React, { useState } from 'react';
import { BrowserRouter as Router, useNavigate, Routes, Route } from 'react-router-dom';
// IMPORTANT: Remove App.css import here, we'll import it conditionally or handle body defaults differently.
import './styles/App.css'; // <-- REMOVE THIS LINE HERE
import './styles/HomePage.css'; // This is for the pre-login landing page
import './styles/AuthForm.css'; // For Login/Register forms
import './styles/Header.css'; // Header will always use its own default styles unless explicitly themed by its parent
import './styles/InboxPage.css'; // This will be themed
import './styles/SideBar.css'; // This will be themed

import Login from './pages/Login';
import Register from './pages/Register';
import Inbox from './pages/InboxPage';
import ProtectedRoute from './components/ProtectedRoute';
import { LabelProvider } from './context/LabelContext';
import { ThemeProvider } from './context/ThemeContext'; // Import ThemeProvider

function HomeContent() {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <div className="logo-section">
        <h1 className="title">Welcome to MyMail</h1>
      </div>

      <div className="auth-buttons">
        <button className="btn-login" onClick={() => navigate('/login')}>Login</button>
        <button className="btn-register" onClick={() => navigate('/register')}>Create account</button>
      </div>
    </div>
  );
}

export default function App() {
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const toggleSidebar = () => {
    setIsSidebarOpen(prevState => !prevState);
  };

  return (
    <div className="App"> {/* This 'App' div will now act as the default background for non-themed pages */}
      <Router>
        <Routes>
          <Route path="/" element={<HomeContent />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/inbox" element={
            // Move ThemeProvider inside ProtectedRoute/Inbox for conditional theming
            <ThemeProvider> {/* THEME PROVIDER MOVED HERE */}
              <LabelProvider>
                <ProtectedRoute>
                  <Inbox isSidebarOpen={isSidebarOpen} toggleSidebar={toggleSidebar} />
                </ProtectedRoute>
              </LabelProvider>
            </ThemeProvider>
          } />
        </Routes>
      </Router>
    </div>
  );
}