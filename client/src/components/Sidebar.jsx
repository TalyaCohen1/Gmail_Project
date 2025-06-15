// src/components/Sidebar.jsx

import React, { useState } from 'react';
import LabelManager from './LabelManager';
import CreateMail from "../components/CreateMail";
import '../styles/SideBar.css';
import { Link } from 'react-router-dom';
// Import all necessary mail service functions
import { getEmails ,getInboxEmails, getDraftEmails, getSentEmails, getEmailLabels, getSpamEmails, getDeletedEmails } from '../services/mailService'; // NEW: Added getSentEmails, getEmails, getEmailLabels

const SideBar = ({ isSidebarOpen, setDisplayedEmails, setDisplayLoading, setDisplayError }) => {
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
      } else if (type === 'filter') {
        // Client-side filtering for labels like Starred, Important, All Mail, Categories
        const allMails = await getEmails(); // Fetch all general mails
        const filteredMails = [];

        // Iterate through all mails and check their labels
        for (const mail of allMails) {
          try {
            const mailLabels = await getEmailLabels(mail.id); // Fetch labels for each mail
            // Check if any of the mail's labels match the target filter label name
            if (mailLabels.some(label => label.name === param)) {
              filteredMails.push(mail);
            }
          } catch (mailLabelError) {
            console.warn(`Could not fetch labels for mail ${mail.id}:`, mailLabelError);
            // Optionally, skip this mail or handle the error specifically
          }
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
    { name: 'Inbox', icon: '/icons/inbox.svg', handler: () => handleSystemLabelClick('api', getInboxEmails), type: 'system' },
    { name: 'Starred', icon: '/icons/starred.svg', handler: () => handleSystemLabelClick('filter', 'Starred'), type: 'system' }, // NEW: Filtered
    { name: 'Snoozed', icon: '/icons/snoozed.svg', handler: () => handleSystemLabelClick('filter', 'Snoozed'), type: 'system' }, // NEW: Filtered
    { name: 'Sent', icon: '/icons/sent.svg', handler: () => handleSystemLabelClick('api', getSentEmails), type: 'system' }, // NEW: Added getSentEmails
    { name: 'Drafts', icon: '/icons/drafts.svg', handler: () => handleSystemLabelClick('api', getDraftEmails), type: 'system' },
  ];

  const moreLabels = [
    { name: 'Important', icon: '/icons/important.svg', handler: () => handleSystemLabelClick('filter', 'Important'), type: 'system' }, // NEW: Filtered
    { name: 'Scheduled', icon: '/icons/scheduled.svg', handler: () => handleSystemLabelClick('filter', 'Scheduled'), type: 'system' }, // NEW: Filtered
    { name: 'All Mail', icon: '/icons/all_mail.svg', handler: () => handleSystemLabelClick('api', getEmails), type: 'system' }, // NEW: Filtered (will show all mails regardless of other labels if mail has "All Mail" label, or if "All Mail" implies no filter)
    { name: 'Spam', icon: '/icons/spam.svg', handler: () => handleSystemLabelClick('api', getSpamEmails), type: 'system' },       // NEW: Filtered
    { name: 'Trash', icon: '/icons/trash.svg', handler: () => handleSystemLabelClick('api', getDeletedEmails), type: 'system' },     // NEW: Filtered
    { name: 'Categories', icon: '/icons/categories.svg', path: '#', type: 'category' }
  ];

  const categorySubLabels = [
    { name: 'Social', icon: '/icons/social.svg', handler: () => handleSystemLabelClick('filter', 'Social'), type: 'system' },       // NEW: Filtered
    { name: 'Updates', icon: '/icons/updates.svg', handler: () => handleSystemLabelClick('filter', 'Updates'), type: 'system' },     // NEW: Filtered
    { name: 'Forums', icon: '/icons/forums.svg', handler: () => handleSystemLabelClick('filter', 'Forums'), type: 'system' },       // NEW: Filtered
    { name: 'Promotions', icon: '/icons/promotions.svg', handler: () => handleSystemLabelClick('filter', 'Promotions'), type: 'system' }, // NEW: Filtered
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
            {item.handler ? ( // Use handler if available
              <button onClick={item.handler} className="sidebar-item sidebar-button-link">
                <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
                {isEffectivelyOpen && <span>{item.name}</span>}
              </button>
            ) : ( // Fallback to Link if no handler (though all defaultLabels now have handlers)
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
                        <React.Fragment key={subItem.name}> {/* Use React.Fragment here */}
                            {subItem.handler ? ( // Check for handler for category sub-labels
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
                <React.Fragment key={item.name}> {/* Use React.Fragment here */}
                    {item.handler ? ( // Check for handler for more labels
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