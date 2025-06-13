// src/pages/InboxPage.jsx
import React from "react";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList"
import "../styles/InboxPage.css";

// Accept isSidebarOpen and toggleSidebar as props
const Inbox = ({ isSidebarOpen, toggleSidebar }) => {
  return (
    <div className="inbox-page">
      {/* Pass toggleSidebar to Header */}
      <Header toggleSidebar={toggleSidebar} />
      <div className="main-content-area">
        {/* Pass isSidebarOpen to Sidebar */}
        <Sidebar isSidebarOpen={isSidebarOpen} />
        <div className="email-list-container">
          <EmailList />
        </div>
      </div>
    </div>
  );
};

export default Inbox;