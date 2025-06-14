import React, { useState, useEffect, useRef } from 'react';
import '../styles/Header.css';
import { useNavigate } from 'react-router-dom';

function Header() {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(null); // or []
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