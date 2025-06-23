// src/components/Header.jsx

import { ThemeContext } from '../context/ThemeContext';
import React, { useState, useEffect, useRef, useContext } from 'react';
import '../styles/Header.css';
import { useNavigate } from 'react-router-dom';
import EditProfilePopup from './Profile';
import { searchEmails } from '../services/mailService'; // Import searchEmails
import { useDisplayEmails } from '../context/DisplayEmailsContext'; // Import useDisplayEmails

function Header({ toggleSidebar }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [isSettingsMenuOpen, setSettingsMenuOpen] = useState(false);
  const { theme, toggleTheme } = useContext(ThemeContext);
  const [showMenu, setShowMenu] = useState(false);
  const [showEditPopup, setShowEditPopup] = useState(false);

  const menuRef = useRef(null);
  const navigate = useNavigate();

  // Access display context functions
  const { setDisplayedEmails, setDisplayLoading, setDisplayError } = useDisplayEmails();

  const fullName = localStorage.getItem('fullName');
  const profileImage = localStorage.getItem('profileImage')
  ? `http://localhost:3000${localStorage.getItem('profileImage')}`
  : 'http://localhost:3000/uploads/default-profile.png';

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setDisplayLoading(true); // Indicate loading
    setDisplayError(null);   // Clear previous errors

    try {
      const data = await searchEmails(searchQuery); // Use searchEmails to get mail objects

      // Set the search results directly to be displayed
      setDisplayedEmails(data);
    } catch (error) {
      console.error('Search error:', error);
      setDisplayError(`Error searching emails: ${error.message}`); // Set error
      setDisplayedEmails([]); // Clear displayed emails on error
    } finally {
      setDisplayLoading(false); // End loading
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
    <>
      <header className="gmail-header">
        <div className="header-left-section">
          <button onClick={toggleSidebar} className="sidebar-toggle-button">
            <img src="/icons/menu.svg" alt="Menu" className="menu-icon" />
          </button>
          <div className="header-logo">
            <img src="/Smail_logo.svg" alt="Gmail Logo" className="gmail-logo-icon" />
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
            <div className="profile-greeting">Hello, {fullName}</div>
            {showMenu && (
              <div className="dropdown-menu">
                <div className="dropdown-item" onClick={() => setShowEditPopup(true)}>Edit Profile</div>
                <div className="dropdown-item" onClick={handleLogout}>Logout</div>
              </div>
            )}
          </div>
        </div>
      </header>
      {showEditPopup && (
        <EditProfilePopup
          currentName={localStorage.getItem('fullName')}
          currentImage={localStorage.getItem('profileImage')}
          onClose={() => setShowEditPopup(false)}
        />
      )}
    </>
  );
}

export default Header;