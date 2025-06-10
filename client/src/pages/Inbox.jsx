import React from "react";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
//import "../styles/Inbox.css";

const Inbox = () => {
  return (
    <div className="inbox-container">
      <Header />
      <Sidebar />
      <div className="inbox-main">
        <h2>Inbox</h2>
        {/* Later: Map through emails here */}
      </div>
    </div>
  );
};

export default Inbox;
