import React, { useState, useEffect } from 'react';
import LabelManager from './LabelManager';
import CreateMail from "../components/CreateMail";
import '../styles/SideBar.css';
import { Link } from 'react-router-dom';
import { useLabels } from '../context/LabelContext';
// Import all necessary mail service functions
import { getEmails, getInboxEmails, getDraftEmails, getSentEmails, getEmailLabels, getSpamEmails, getDeletedEmails } from '../services/mailService';

const SideBar = ({ isSidebarOpen, setDisplayedEmails, setDisplayLoading, setDisplayError }) => {
  const [isHovered, setIsHovered] = useState(false);
  const [showMoreLabels, setShowMoreLabels] = useState(false);
  const [showCategoriesSubLabels, setShowCategoriesSubLabels] = useState(false);
  const [showCreateMail, setShowCreateMail] = useState(false);
  const [inboxCount, setInboxCount] = useState(0); // State to hold inbox email count
  const [draftsCount, setDraftsCount] = useState(0); // State to hold drafts count
  const [sentCount, setSentCount] = useState(0); // State to hold sent emails count

  const { labels, fetchMailsForLabel } = useLabels();

  const isEffectivelyOpen = isSidebarOpen || isHovered;

  // Fetch inbox count on component mount
  useEffect(() => {
    const fetchInboxCount = async () => {
      try {
        const emails = await getInboxEmails();
        setInboxCount(emails.length);
      } catch (err) {
        console.error("Error fetching inbox count:", err);
        setInboxCount(0); // Reset count on error
      }
    };
    fetchInboxCount();
  }, []); // Empty dependency array means this runs once on mount

  const handleNewEmail = () => {
    if (showCreateMail) {
      setShowCreateMail(false);
      setTimeout(() => setShowCreateMail(true), 0);
    } else {
      setShowCreateMail(true);
    }
  };

  /**
   * Handles clicks on system labels, either by direct API call or client-side filtering.
   * @param {'api' | 'filter'} type - The type of action: 'api' for direct fetch, 'filter' for client-side filtering.
   * @param {Function | string} param - If type is 'api', this is the fetch function (e.g., getInboxEmails).
   * If type is 'filter', this is the label name to filter by (e.g., 'Starred').
   */
  const handleSystemLabelClick = async (type, param) => {
    setDisplayLoading(true);
    setDisplayError(null);
    try {
      let emailsToDisplay = [];
      if (type === 'api') {
        // Direct API call for labels like Inbox, Drafts, Sent
        emailsToDisplay = await param();
        // If it's the Inbox, update the count
        if (param === getInboxEmails) {
          setInboxCount(emailsToDisplay.length);
        }
      } else if (type === 'filter') {
        const targetLabel = labels.find(label => label.name === param);
        let filteredMails = [];
        if (targetLabel) {
          filteredMails = await fetchMailsForLabel(targetLabel.id);
        } else {
          console.warn(`Label '${param}' not found in LabelContext for filter type.`);
          filteredMails = [];
        }
        emailsToDisplay = filteredMails;
      }
      setDisplayedEmails(emailsToDisplay);
    } catch (err) {
      setDisplayError(`Error fetching emails for ${param || 'label'}: ${err.message}`);
      console.error("Error in handleSystemLabelClick:", err);
      setDisplayedEmails([]);
    } finally {
      setDisplayLoading(false);
    }
  };

  const defaultLabels = [
    { name: 'Inbox', icon: '/icons/inbox.svg', handler: () => handleSystemLabelClick('api', getInboxEmails), type: 'system', count: inboxCount }, // Added count property
    { name: 'Starred', icon: '/icons/starred.svg', handler: () => handleSystemLabelClick('filter', 'Starred'), type: 'system' },
    { name: 'Sent', icon: '/icons/sent.svg', handler: () => handleSystemLabelClick('api', getSentEmails), type: 'system' },
    { name: 'Drafts', icon: '/icons/drafts.svg', handler: () => handleSystemLabelClick('api', getDraftEmails), type: 'system' },
  ];

  const moreLabels = [
    { name: 'Important', icon: '/icons/important.svg', handler: () => handleSystemLabelClick('filter', 'Important'), type: 'system' },
    { name: 'All Mail', icon: '/icons/all_mail.svg', handler: () => handleSystemLabelClick('api', getEmails), type: 'system' },
    { name: 'Spam', icon: '/icons/spam.svg', handler: () => handleSystemLabelClick('api', getSpamEmails), type: 'system' },
    { name: 'Trash', icon: '/icons/trash.svg', handler: () => handleSystemLabelClick('api', getDeletedEmails), type: 'system' },
    { name: 'Categories', icon: '/icons/categories.svg', path: '#', type: 'category' }
  ];

  const categorySubLabels = [
    { name: 'Social', icon: '/icons/social.svg', handler: () => handleSystemLabelClick('filter', 'Social'), type: 'system' },
    { name: 'Updates', icon: '/icons/updates.svg', handler: () => handleSystemLabelClick('filter', 'Updates'), type: 'system' },
    { name: 'Forums', icon: '/icons/forums.svg', handler: () => handleSystemLabelClick('filter', 'Forums'), type: 'system' },
    { name: 'Promotions', icon: '/icons/promotions.svg', handler: () => handleSystemLabelClick('filter', 'Promotions'), type: 'system' },
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
          <React.Fragment key={item.name}>
            {item.handler ? (
              <button onClick={item.handler} className="sidebar-item sidebar-button-link">
                <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                {isEffectivelyOpen && (
                  <span>
                    {item.name}
                    {item.name === 'Inbox' && item.count > 0 && (
                      <span className="mail-count"> ({item.count})</span>
                    )}
                  </span>
                )}
              </button>
            ) : (
              <Link to={item.path} className="sidebar-item">
                <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                {isEffectivelyOpen && <span>{item.name}</span>}
              </Link>
            )}
          </React.Fragment>
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
                    className="sidebar-item category-toggle"
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
                        <React.Fragment key={subItem.name}>
                          {subItem.handler ? (
                            <button onClick={subItem.handler} className="sidebar-item sub-label-item">
                              <img src={subItem.icon} alt={subItem.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                              {isEffectivelyOpen && <span>{subItem.name}</span>}
                            </button>
                          ) : (
                            <Link to={subItem.path} className="sidebar-item sub-label-item">
                              <img src={subItem.icon} alt={subItem.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                              {isEffectivelyOpen && <span>{subItem.name}</span>}
                            </Link>
                          )}
                        </React.Fragment>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <React.Fragment key={item.name}>
                  {item.handler ? (
                    <button onClick={item.handler} className="sidebar-item">
                      <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                      {isEffectivelyOpen && <span>{item.name}</span>}
                    </button>
                  ) : (
                    <Link to={item.path} className="sidebar-item">
                      <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                      {isEffectivelyOpen && <span>{item.name}</span>}
                    </Link>
                  )}
                </React.Fragment>
              )}
            </React.Fragment>
          ))}
        </div>
      )}
      {isEffectivelyOpen && (
        <LabelManager
          setDisplayedEmails={setDisplayedEmails}
          setDisplayLoading={setDisplayLoading}
          setDisplayError={setDisplayError}
        />
      )}
    </div>
  );
};

export default SideBar;