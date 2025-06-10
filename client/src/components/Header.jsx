import React, { useState } from 'react';
import '../styles/Header.css';
import LogOut from './LogOut';

function Header() {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null); // or []

  const fullName = localStorage.getItem('fullName');
  const profileImage = localStorage.getItem('profileImage') || '/default-profile.png';


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

  return (
    <header className="mailblossom-header">
      <div className="left-section">
      <span role="img" aria-label="flower" className="flower-icon">🌸</span>
      <h1>MailBlossom</h1>
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

      <div className="profile-section">
        <img
          src={profileImage}
          alt="Profile"
          width="40"
          height="40"
          style={{ borderRadius: '50%', marginRight: '10px' }}
        />
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
