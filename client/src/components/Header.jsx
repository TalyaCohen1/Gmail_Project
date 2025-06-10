import React, { useState } from 'react';
import '../styles/Header.css';

function Header() {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null); // or []

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    try {
      const response = await fetch(`http://localhost:3000/mails/search/${encodeURIComponent(searchQuery)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        // add auth headers here if needed
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
    <header className="gmail-header">
      <div className="gmail-logo-container">
        {/* Changed to use the actual image file */}
        <img src="/gmail_logo.png" alt="Gmail Logo" className="gmail-icon" /> {/* */}
        {/*<h1>Gmail</h1> */}
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