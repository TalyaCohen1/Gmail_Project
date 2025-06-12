import React from 'react';
import LabelManager from './LabelManager'; // adjust the path if needed
import '../styles/SideBar.css'; // optional, your sidebar styling
import CreateMail from "../components/CreatMail";

const SideBar = () => {
    const [showCreateMail, setShowCreateMail] = React.useState(false);

    const handleNewEmail = () => {
        if (showCreateMail) {
            // אם זה כבר פתוח, סגור ופתח מחדש (reset)
            setShowCreateMail(false);
            setTimeout(() => setShowCreateMail(true), 0);
        } else {
            // אם זה סגור, פתח
            setShowCreateMail(true);
        }
    };

  return (
    <div className="sidebar">
      <h1>Side bar</h1>
      {/* You can add other sidebar items here, e.g. Inbox, Sent, etc. */}


      {/* Label manager shows all labels with add/edit/delete */}
      <LabelManager />

      <div className="inbox-main p-4">
                <div className="flex justify-between items-center mb-4">
                    <button 
                        onClick={handleNewEmail} 
                        className="px-4 py-2 bg-blue-600 text-white rounded"
                    >
                        New Email
                    </button>
                </div>
            </div>
            
            {showCreateMail && (
                <CreateMail 
                    onSend={() => {
                        // אחרי שליחה, סגור את החלון
                        setShowCreateMail(false);
                    }} 
                />
            )}
        </div>
  );
};

export default SideBar;
