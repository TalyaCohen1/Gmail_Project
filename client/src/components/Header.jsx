// src/components/Header.jsx
import React, { useState } from 'react';
import '../styles/Header.css';
import LogOut from './LogOut';

// Receive toggleSidebar as a prop
function Header({ toggleSidebar }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null);

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
      const mail_ids = data.map(mail => mail.id);
      setSearchResults(data);

      console.log('Search results:', data);
    } catch (error) {
      console.error('Search error:', error);
    }
  };

  return (
    <header className="gmail-header">
      {/* New sidebar toggle button */}
      <button onClick={toggleSidebar} className="sidebar-toggle-button">
        <img src="/icons/menu.svg" alt="Menu" className="menu-icon" /> {/* Make sure you have a menu.svg icon */}
      </button>

      <div className="gmail-logo-container">
        <img src="/gmail_logo.png" alt="Gmail Logo" className="gmail-icon" />
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

      <div className="profile-section">
        <span className="profile-name">{fullName}</span>
        <LogOut />
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