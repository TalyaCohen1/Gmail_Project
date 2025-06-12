import React, { useState } from 'react';
import LabelManager from './LabelManager';
import CreateMail from "../components/CreateMail";
import '../styles/SideBar.css';
import { Link } from 'react-router-dom';

const SideBar = () => {
  const [showMoreLabels, setShowMoreLabels] = useState(false);
  const [showCategoriesSubLabels, setShowCategoriesSubLabels] = useState(false);
  const [showCreateMail, setShowCreateMail] = React.useState(false);

  const handleNewEmail = () => {
        if (showCreateMail) {
          //reset
            setShowCreateMail(false);
            setTimeout(() => setShowCreateMail(true), 0);
        } else {
          //open
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
    { name: 'Categories', icon: '/icons/categories.svg', type: 'category' },
  ];

  const categorySubLabels = [
    { name: 'Social', icon: '/icons/social.svg', path: '/categories/social' },
    { name: 'Updates', icon: '/icons/updates.svg', path: '/categories/updates' },
    { name: 'Forums', icon: '/icons/forums.svg', path: '/categories/forums' },
    { name: 'Promotions', icon: '/icons/promotions.svg', path: '/categories/promotions' },
  ];

  return (
    <div className="sidebar">
      <button className="compose-button"  onClick={handleNewEmail}>
        <img
          src="/icons/all_mail.svg"
          alt="Compose"
          className="compose-icon"
          onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=P" }}
        />
        <span>Compose</span>
      </button>
      {showCreateMail && (
                      <CreateMail 
                          onSend= {() => setShowCreateMail(false)}
                          onClose= {() => setShowCreateMail(false)}
                      />
                  )}
      {/* Default Labels Section */}
      <div className="sidebar-section">
        {defaultLabels.map((item) => (
          <Link to={item.path} key={item.name} className="sidebar-item">
            <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
            <span>{item.name}</span>
          </Link>
        ))}
      </div>

      {showMoreLabels && (
        <div className="sidebar-section more-labels">
          {moreLabels.map((item) => (
            <React.Fragment key={item.name}>
              {item.type === 'category' ? (
                <button
                  onClick={() => setShowCategoriesSubLabels(!showCategoriesSubLabels)}
                  className={`sidebar-item ${showCategoriesSubLabels ? 'active' : ''} categories-button`}
                >
                  {/* Moved arrow icon to the beginning */}
                  <img
                    src={showCategoriesSubLabels ? '/icons/arrow_drop_down.svg' : '/icons/arrow_right.svg'}
                    alt={showCategoriesSubLabels ? 'Collapse' : 'Expand'}
                    className="category-toggle-icon"
                    onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=V" }}
                  />
                  <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                  <span>{item.name}</span>
                </button>
              ) : (
                <Link to={item.path} key={item.name} className="sidebar-item">
                  <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                  <span>{item.name}</span>
                </Link>
              )}

              {item.type === 'category' && showCategoriesSubLabels && (
                <div className="category-sub-labels-container">
                  {categorySubLabels.map((subItem) => (
                    <Link to={subItem.path} key={subItem.name} className="sidebar-item sub-label-item">
                      <img src={subItem.icon} alt={subItem.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                      <span>{subItem.name}</span>
                    </Link>
                  ))}
                </div>
              )}
            </React.Fragment>
          ))}
        </div>
      )}

      <button
        onClick={() => setShowMoreLabels(!showMoreLabels)}
        className="sidebar-item more-button"
      >
        <img
          src={showMoreLabels ? '/icons/less.svg' : '/icons/more.svg'}
          alt={showMoreLabels ? 'Less' : 'More'}
          className="sidebar-icon"
          onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=..." }}
        />
        <span>{showMoreLabels ? 'Less' : 'More'}</span>
      </button>

      <LabelManager />
    </div>
  );
};

export default SideBar;