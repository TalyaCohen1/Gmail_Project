// src/components/Sidebar.jsx

import React, { useState } from 'react';
import LabelManager from './LabelManager';
import CreateMail from "../components/CreateMail";
import '../styles/SideBar.css';
import { Link } from 'react-router-dom';

const SideBar = ({ isSidebarOpen }) => {
  const [isHovered, setIsHovered] = useState(false);
  const [showMoreLabels, setShowMoreLabels] = useState(false);
  const [showCategoriesSubLabels, setShowCategoriesSubLabels] = useState(false);
  const [showCreateMail, setShowCreateMail] = useState(false);

  const isEffectivelyOpen = isSidebarOpen || isHovered;

  const handleNewEmail = () => {
    if (showCreateMail) {
      setShowCreateMail(false);
      setTimeout(() => setShowCreateMail(true), 0);
    } else {
      setShowCreateMail(true);
    }
  };

  const defaultLabels = [
    { name: 'Inbox', icon: '/icons/inbox.svg', path: '/inbox' },
    { name: 'Starred', icon: '/icons/starred.svg', path: '/starred' },
    { name: 'Snoozed', icon: '/icons/snoozed.svg', path: '/snoozed' },
    { name: 'Sent', icon: '/icons/sent.svg', path: '/sent' },
    { name: 'Drafts', icon: '/icons/drafts.svg', path: '/drafts' },
  ];

  const moreLabels = [
    { name: 'Important', icon: '/icons/important.svg', path: '/important' },
    { name: 'Scheduled', icon: '/icons/scheduled.svg', path: '/scheduled' },
    { name: 'All Mail', icon: '/icons/all_mail.svg', path: '/all-mail' },
    { name: 'Spam', icon: '/icons/spam.svg', path: '/spam' },
    { name: 'Trash', icon: '/icons/trash.svg', path: '/trash' },
    { name: 'Categories', icon: '/icons/categories.svg', path: '#', type: 'category' }
  ];

  const categorySubLabels = [
    { name: 'Social', icon: '/icons/social.svg', path: '/social' },
    { name: 'Updates', icon: '/icons/updates.svg', path: '/updates' },
    { name: 'Forums', icon: '/icons/forums.svg', path: '/forums' },
    { name: 'Promotions', icon: '/icons/promotions.svg', path: '/promotions' },
  ];

  return (
    <div
      className={`sidebar ${isEffectivelyOpen ? '' : 'sidebar--closed'}`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <button className={`compose-button ${isEffectivelyOpen ? '' : 'compose-button--closed'}`} onClick={handleNewEmail}>
        <img src="/icons/new_mail.svg" alt="Compose" className="compose-icon" />
        {isEffectivelyOpen && <span>Compose</span>}
      </button>

      {showCreateMail && <CreateMail onClose={() => setShowCreateMail(false)} />}

      <div className="sidebar-section main-labels">
        {defaultLabels.map((item) => (
          <Link to={item.path} key={item.name} className="sidebar-item">
            <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
            {isEffectivelyOpen && <span>{item.name}</span>}
          </Link>
        ))}
      </div>

      <button
        onClick={() => setShowMoreLabels(!showMoreLabels)}
        className="sidebar-item more-button"
      >
        <img
          src={showMoreLabels ? '/icons/less.svg' : '/icons/more.svg'}
          alt={showMoreLabels ? 'Less' : 'More'}
          className="sidebar-icon"
          onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }}
        />
        {isEffectivelyOpen && <span>{showMoreLabels ? 'Less' : 'More'}</span>}
      </button>

      {showMoreLabels && (
        <div className="sidebar-section more-labels">
          {moreLabels.map((item) => (
            <React.Fragment key={item.name}>
              {item.type === 'category' ? (
                <>
                  <button
                    onClick={() => setShowCategoriesSubLabels(!showCategoriesSubLabels)}
                    className="sidebar-item category-toggle" // Removed `showCategoriesSubLabels ? 'active' : ''`
                  >
                    <img
                      src={showCategoriesSubLabels ? '/icons/arrow_drop_down.svg' : '/icons/arrow_right.svg'}
                      alt="Toggle Categories"
                      className="category-arrow-icon"
                    />
                    <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                    {isEffectivelyOpen && <span>{item.name}</span>}
                  </button>
                  {showCategoriesSubLabels && (
                    <div className="category-sub-labels-container">
                      {categorySubLabels.map((subItem) => (
                        <Link to={subItem.path} key={subItem.name} className="sidebar-item sub-label-item">
                          <img src={subItem.icon} alt={subItem.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                          {isEffectivelyOpen && <span>{subItem.name}</span>}
                        </Link>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <Link to={item.path} key={item.name} className="sidebar-item">
                  <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                  {isEffectivelyOpen && <span>{item.name}</span>}
                </Link>
              )}
            </React.Fragment>
          ))}
        </div>
      )}
      {isEffectivelyOpen && (
        <LabelManager />
      )}
    </div>
  );
};

export default SideBar;