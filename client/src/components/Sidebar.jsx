import React from 'react';
import LabelManager from './LabelManager'; // adjust the path if needed
import '../styles/SideBar.css'; // optional, your sidebar styling

const SideBar = () => {
  return (
    <div className="sidebar">
      <h1>Side bar</h1>
      {/* You can add other sidebar items here, e.g. Inbox, Sent, etc. */}

      {/* Label manager shows all labels with add/edit/delete */}
      <LabelManager />
    </div>
  );
};

export default SideBar;
