
// src/components/Header.jsx
import { ThemeContext } from '../context/ThemeContext'; // Import ThemeContext
import React, { useState, useEffect, useRef, useContext } from 'react';
import '../styles/Header.css';
import { useNavigate } from 'react-router-dom';

function Header({ toggleSidebar }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [isSettingsMenuOpen, setSettingsMenuOpen] = useState(false); // State for settings menu
  const { theme, toggleTheme } = useContext(ThemeContext); // Use the theme context
  const [showMenu, setShowMenu] = useState(false);

  const menuRef = useRef(null);
  const navigate = useNavigate();

  const fullName = localStorage.getItem('fullName');
  const profileImage = localStorage.getItem('profileImage')
  ? `http://localhost:3000${localStorage.getItem('profileImage')}`
  : 'http://localhost:3000/uploads/default-profile.png';

  console.log("2: Profile Image:", profileImage);



  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    try {
      const response = await fetch(`http://localhost:3000/mails/search/${encodeURIComponent(searchQuery)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const data = await response.json();
      setSearchResults(data); // store results

      // TODO: You can either display them here or lift state up via props

      console.log('Search results:', data);
    } catch (error) {
      console.error('Search error:', error);
    }
  };
   const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleClickOutside = (e) => {
    if (menuRef.current && !menuRef.current.contains(e.target)) {
      setShowMenu(false);
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);


  return (
    <header className="gmail-header">
      <div className="header-left-section">
        <button onClick={toggleSidebar} className="sidebar-toggle-button">
          <img src="/icons/menu.svg" alt="Menu" className="menu-icon" />
        </button>
        {/* Moved after the menu button */}
        <div className="header-logo">
          {/* Changed className from gmail-icon to gmail-logo-icon */}
          <img src="/gmail_logo.png" alt="Gmail Logo" className="gmail-logo-icon" />
        </div>
      </div>

      <form onSubmit={handleSearch} className="header-search-form">
        <input
          type="text"
          placeholder="Search mails..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="header-search-input"
        />
        <button type="submit" className="header-search-button">Search</button>
      </form>

      <div className="header-right-section">
        {/* Moved before profile-section */}
        <div className="settings-container">
          <button onClick={() => setSettingsMenuOpen(!isSettingsMenuOpen)} className="settings-button">
            <img src="/icons/settings.svg" alt="Settings" className="settings-icon" />
          </button>
          {isSettingsMenuOpen && (
            <div className="settings-menu">
              <button onClick={toggleTheme} className="theme-toggle-button">
                {theme === 'light' ? 'Enable Dark Mode' : 'Disable Dark Mode'}
              </button>
            </div>
          )}
        </div>
         <div className="profile-section" ref={menuRef}>
        <img
          src={profileImage}
          alt="Profile"
          width="40"
          height="40"
          style={{ borderRadius: '50%', cursor: 'pointer' }}
          onClick={() => setShowMenu(prev => !prev)}
        />
         {showMenu && (
          <div className="dropdown-menu">
            <div className="dropdown-item" onClick={() => navigate('/profile')}>Edit Profile</div>
            <div className="dropdown-item" onClick={handleLogout}>Logout</div>
          </div>
        )}
      </div>
      </div>

      {searchResults && (
        <div className="search-results">
          <h3>Results:</h3>
          <ul>
            {searchResults.length === 0 && <li>No mails found.</li>}
            {searchResults.map(mail => (
              <li key={mail.id}>
                <strong>{mail.subject}</strong> - {mail.snippet || mail.body.slice(0, 100)}...
              </li>
            ))}
          </ul>
        </div>
      )}
    </header>
  );
}

export default Header;