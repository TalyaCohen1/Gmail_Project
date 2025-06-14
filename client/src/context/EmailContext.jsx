import React, { createContext, useState } from 'react';
import { getEmailById } from '../services/emailService';

// Context to manage fetching and storing mails by ID list
export const SelectedMailsContext = createContext({
  selectedMailIds: [],
  mails: [],
  loading: false,
  error: null,
  fetchMailsByIds: async (ids) => {}
});

export function SelectedMailsProvider({ children }) {
    const [selectedMailIds, setSelectedMailIds] = useState([]);
    const [mails, setMails] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Fetch mails for a given array of IDs and store them in state
     * @param {Array<string|number>} ids
     */
    const fetchMailsByIds = async (ids) => {
        setSelectedMailIds(ids);
        setLoading(true);
        setError(null);
        try {
            // Parallel fetch all selected mails
            const results = await Promise.all(ids.map(id => getEmailById(id)));
            setMails(results);
        } catch (err) {
            console.error('Error fetching mails by IDs:', err);
            setError(err.message);
            setMails([]);
        } finally {
        setLoading(false);
        }
    };

    return (
        <SelectedMailsContext.Provider
        value={{ selectedMailIds, mails, loading, error, fetchMailsByIds }}
        >
            {children}
        </SelectedMailsContext.Provider>
    );
}