// src/components/Header.jsx
import React, { useState, useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext'; // Import ThemeContext
import '../styles/Header.css';
import LogOut from './LogOut';

// Receive toggleSidebar as a prop
function Header({ toggleSidebar }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);
  const [isSettingsMenuOpen, setSettingsMenuOpen] = useState(false); // State for settings menu
  const { theme, toggleTheme } = useContext(ThemeContext); // Use the theme context

  const fullName = localStorage.getItem('fullName');
  const rawImage = localStorage.getItem('profileImage');
  const profileImage = (!rawImage || rawImage === 'undefined')
    ? '/uploads/default-profile.png'
    : rawImage;

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
      setSearchResults(data);
    } catch (error) {
      console.error('Search error:', error);
    }
  };

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
        <button type="submit" className="header-search-button">
          <img src="/icons/search.svg" alt="Search" className="search-icon" />
        </button>
        <input
          type="text"
          placeholder="Search mails..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="header-search-input"
        />
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
        <div className="profile-section">
          <span className="profile-name">{fullName}</span>
          <LogOut />
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