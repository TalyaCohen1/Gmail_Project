// src/components/Sidebar.jsx

import React, { useState, useEffect, forwardRef, useImperativeHandle, useCallback, useMemo } from 'react';
import LabelManager from './LabelManager';
import CreateMail from "../components/CreateMail";
import '../styles/SideBar.css';
import { Link } from 'react-router-dom';
import { useLabels } from '../context/LabelContext';
// Import all necessary mail service functions
import { getEmails, getInboxEmails, getDraftEmails, getSentEmails, getEmailLabels, getSpamEmails, getDeletedEmails, getStarredEmails, getImportantEmails } from '../services/mailService';

const SideBar = forwardRef(({ isSidebarOpen, setDisplayedEmails, setDisplayLoading, setDisplayError, setCurrentView }, ref) => {
  const [isHovered, setIsHovered] = useState(false);
  const [showMoreLabels, setShowMoreLabels] = useState(false);
  const [showCategoriesSubLabels, setShowCategoriesSubLabels] = useState(false);
  const [showCreateMail, setShowCreateMail] = useState(false);
  const [inboxCount, setInboxCount] = useState(0); // State to hold inbox email count
  const [draftsCount, setDraftsCount] = useState(0); // State to hold drafts count
  const [sentCount, setSentCount] = useState(0); // State to hold sent emails count
  const [spamCount, setSpamCount] = useState(0); // State to hold spam emails count
  const [starredCount, setStarredCount] = useState(0); // State to hold starred emails count
  const [importantCount, setImportantCount] = useState(0); // State to hold important emails count
  const [deletedCount, setDeletedCount] = useState(0); // State to hold deleted emails count
  const [categoryCounts, setCategoryCounts] = useState({}); // State to hold counts for category labels

  const { labels, fetchMailsForLabel } = useLabels();

  const isEffectivelyOpen = isSidebarOpen || isHovered;

  // Fetch counts on component mount and when labels change
  const fetchCounts = async () => {
  try {
    const inbox = await getInboxEmails();
    setInboxCount(inbox.length);

    const drafts = await getDraftEmails();
    setDraftsCount(drafts.length);

    const sent = await getSentEmails();
    setSentCount(sent.length);

    const spam = await getSpamEmails();
    setSpamCount(spam.length);

    const starred = await getStarredEmails();
    setStarredCount(starred.length);

    const important = await getImportantEmails();
    setImportantCount(important.length);

    const deleted = await getDeletedEmails();
    setDeletedCount(deleted.length);

    if (labels.length > 0) {
      const newCategoryCounts = {};
      const categoryNames = ['Social', 'Updates', 'Forums', 'Promotions'];
      for (const name of categoryNames) {
        const label = labels.find(l => l.name === name);
        if (label) {
          const mails = await fetchMailsForLabel(label.id);
          newCategoryCounts[name] = mails ? mails.length : 0;
        }
      }
      setCategoryCounts(newCategoryCounts);
    }
  } catch (err) {
    console.error("Error fetching email counts:", err);
    setInboxCount(0);
    setDraftsCount(0);
    setSentCount(0);
    setSpamCount(0);
    setStarredCount(0);
    setImportantCount(0);
    setDeletedCount(0);
    setCategoryCounts({});
  }
};

useEffect(() => {
  fetchCounts();
}, [labels]); // Depend on 'labels' to re-run when labels context loads/changes

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
  const handleSystemLabelClick = useCallback(async (type, param) => {
    setDisplayLoading(true);
    setDisplayError(null);
    try {
      let emailsToDisplay = [];
      if (type === 'api') {
        // Direct API call for labels like Inbox, Drafts, Sent
        emailsToDisplay = await param();
        // Update specific counts based on the function called
        if (param === getInboxEmails) {
          setInboxCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('inbox'); 
        } else if (param === getDraftEmails) {
          setDraftsCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('drafts');
        } else if (param === getSentEmails) {
          setSentCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('sent');
        } else if (param === getSpamEmails) {
          setSpamCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('spam');
        } else if (param === getStarredEmails) {
          setStarredCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('starred');
        } else if (param === getImportantEmails) {
          setImportantCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('important');
        } else if (param === getDeletedEmails) {
          setDeletedCount(emailsToDisplay.length);
          setCurrentView && setCurrentView('deleted');
        } else if (param === getEmails) {
          setCurrentView && setCurrentView('all');  
        }
      } else if (type === 'filter') {
        const lowerCaseParam = param.toLowerCase();
        const targetLabel = labels.find(label => label.name.toLowerCase() === lowerCaseParam);

        let filteredMails = [];
        if (targetLabel) {
            filteredMails = await fetchMailsForLabel(targetLabel.id);

            if (['Social', 'Updates', 'Forums', 'Promotions'].includes(param)) {
                setCategoryCounts(prev => ({ ...prev, [param]: filteredMails.length }));
            }
            setCurrentView(lowerCaseParam);
        } else {
            console.warn(`DEBUG: Label '${param}' not found in LabelContext for filter type.`);
            filteredMails = [];
        }
          emailsToDisplay = filteredMails;
      }
      setDisplayedEmails(emailsToDisplay);
    } catch (err) {
        setDisplayError(`Error fetching emails for ${param || 'label'}: ${err.message}`);
        console.error("DEBUG: Error in handleSystemLabelClick:", err);
        setDisplayedEmails([]);
    } finally {
        setDisplayLoading(false);
    }
}, [
    labels, setCurrentView, fetchMailsForLabel, setDisplayedEmails, setDisplayLoading, setDisplayError, setCategoryCounts,
    // ... וכל שאר פונקציות ה-getXEmails שהיא משתמשת בהן
    getInboxEmails, getSentEmails, getDraftEmails, getSpamEmails, getDeletedEmails, getImportantEmails, getStarredEmails
]);

  const defaultLabels = [
    { name: 'Inbox', icon: '/icons/inbox.svg', handler: () => handleSystemLabelClick('api', getInboxEmails), type: 'system', count: inboxCount },
    { name: 'Starred', icon: '/icons/starred.svg', handler: () => handleSystemLabelClick('api', getStarredEmails), type: 'system', count: starredCount },
    { name: 'Sent', icon: '/icons/sent.svg', handler: () => handleSystemLabelClick('api', getSentEmails), type: 'system', count: sentCount },
    { name: 'Drafts', icon: '/icons/drafts.svg', handler: () => handleSystemLabelClick('api', getDraftEmails), type: 'system', count: draftsCount },
  ];

  const moreLabels = [
    { name: 'Important', icon: '/icons/important.svg', handler: () => handleSystemLabelClick('api', getImportantEmails), type: 'system', count: importantCount },
    { name: 'All Mail', icon: '/icons/all_mail.svg', handler: () => handleSystemLabelClick('api', getEmails), type: 'system', count: inboxCount + sentCount },
    { name: 'Spam', icon: '/icons/spam.svg', handler: () => handleSystemLabelClick('api', getSpamEmails), type: 'system', count: spamCount },
    { name: 'Trash', icon: '/icons/trash.svg', handler: () => handleSystemLabelClick('api', getDeletedEmails), type: 'system', count: deletedCount },
    { name: 'Categories', icon: '/icons/categories.svg', path: '#', type: 'category' }
  ];

  const categorySubLabels = useMemo(() => [
    { name: 'Social', icon: '/icons/social.svg', handler: () => handleSystemLabelClick('filter', 'Social'), type: 'system', count: categoryCounts['Social'] },
    { name: 'Updates', icon: '/icons/updates.svg', handler: () => handleSystemLabelClick('filter', 'Updates'), type: 'system', count: categoryCounts['Updates'] },
    { name: 'Forums', icon: '/icons/forums.svg', handler: () => handleSystemLabelClick('filter', 'Forums'), type: 'system', count: categoryCounts['Forums'] },
    { name: 'Promotions', icon: '/icons/promotions.svg', handler: () => handleSystemLabelClick('filter', 'Promotions'), type: 'system', count: categoryCounts['Promotions'] },
  ], [handleSystemLabelClick, categoryCounts]);

  const renderLabelItem = (item) => (
    <React.Fragment key={item.name}>
      {item.handler ? (
        <button 
        // onClick={item.handler} 
          onClick={item.handler} className="sidebar-item sidebar-button-link">
          <img src={item.icon} alt={item.name} className="sidebar-icon" onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/24x24/cccccc/000000?text=?" }} />
          {isEffectivelyOpen && (
            <span>
              {item.name}
              {item.count !== undefined && item.count >= 0 && (
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
  );

  useImperativeHandle(ref, () => ({
    refreshCounts: fetchCounts
  }));

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
        {defaultLabels.map(renderLabelItem)}
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
                      {categorySubLabels.map(renderLabelItem)}
                    </div>
                  )}
                </>
              ) : (
                renderLabelItem(item)
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
            setCurrentView={setCurrentView}
        />
      )}
    </div>
  );
});

export default SideBar;