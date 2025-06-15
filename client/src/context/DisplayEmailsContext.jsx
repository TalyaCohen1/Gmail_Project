// src/context/DisplayEmailsContext.jsx

import React, { createContext, useState, useContext } from 'react';

const DisplayEmailsContext = createContext();

export const DisplayEmailsProvider = ({ children }) => {
    const [displayedEmails, setDisplayedEmails] = useState([]);
    const [displayLoading, setDisplayLoading] = useState(false);
    const [displayError, setDisplayError] = useState(null);

    return (
        <DisplayEmailsContext.Provider
            value={{
                displayedEmails,
                setDisplayedEmails,
                displayLoading,
                setDisplayLoading,
                displayError,
                setDisplayError
            }}
        >
            {children}
        </DisplayEmailsContext.Provider>
    );
};

export const useDisplayEmails = () => {
    const context = useContext(DisplayEmailsContext);
    if (!context) {
        throw new Error('useDisplayEmails must be used within a DisplayEmailsProvider');
    }
    return context;
};