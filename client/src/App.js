// App.js

import React, { useState } from 'react'; // 1. Import useState
import { BrowserRouter as Router, useNavigate, Routes, Route } from 'react-router-dom';
import './styles/HomePage.css';
import Login from './pages/Login';
import Register from './pages/Register';
import Inbox from './pages/InboxPage';
import ProtectedRoute from './components/ProtectedRoute';
import { LabelProvider } from './context/LabelContext';

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
  // 2. Add state for the sidebar
  const [isSidebarOpen, setIsSidebarOpen] = useState(true); // Default to open
  const toggleSidebar = () => {
    setIsSidebarOpen(prevState => !prevState);
  };

  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={<HomeContent />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/inbox" element={
            <LabelProvider>
              <ProtectedRoute>
                {/* 3. Pass the state and function as props */}
                <Inbox isSidebarOpen={isSidebarOpen} toggleSidebar={toggleSidebar} />
              </ProtectedRoute>
            </LabelProvider>
          } />
        </Routes>
      </Router>
    </div>
  );
}