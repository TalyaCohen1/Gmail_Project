import React from "react";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import EmailList from "../components/EmailList"
import EmailListItem from "../components/EmailListItem";
// import "../styles/InboxPage.css";


const Inbox = () => {
  return (
    <div className="inbox-container">
      <Header />
      <Sidebar />
      <EmailList />
    </div>
  );
};

export default Inbox;