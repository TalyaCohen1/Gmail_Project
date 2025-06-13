import React, { useState } from 'react';
import '../styles/Header.css';
import LogOut from './LogOut';


function Header() {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null); // or []

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
      const mail_ids = data.map(mail => mail.id); // Extract mail IDs
      setSearchResults(data); // store results

      // TODO: You can either display them here or lift state up via props

      console.log('Search results:', data);
    } catch (error) {
      console.error('Search error:', error);
    }
  };

  return (
    <header className="gmail-header">
      <button className="sidebar-toggle-button">
        <img src="/icons/menu.svg" alt="Menu" className="menu-icon" /> {/* Make sure you have a menu.svg icon */}
      </button>
      <div className="gmail-logo-container">
        {/* Changed to use the actual image file */}
        <img src="/gmail_logo.png" alt="Gmail Logo" className="gmail-icon" /> {/* */}
        {/*<h1>Gmail</h1> */}
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
        {/* <img
          src={profileImage}
          alt="Profile"
          width="40"
          height="40"
          style={{ borderRadius: '50%', marginRight: '10px' }}
        /> */}
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