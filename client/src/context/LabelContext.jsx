// src/context/LabelContext.jsx

import React, { createContext, useState, useEffect, useContext } from 'react';
import * as labelService from '../services/labelService'; // Import the service

const LabelContext = createContext();

export const LabelProvider = ({ children }) => {
    const [labels, setLabels] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchLabels = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await labelService.getLabels();
            if (Array.isArray(data)) {
                setLabels(data);
            } else {
                console.error('Expected array but got:', data);
                setLabels([]);
                setError('Invalid data format received from server');
            }
        } catch (err) {
            console.error('Could not fetch labels:', err);
            setLabels([]);
            setError(`Failed to fetch labels: ${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    // Fetch labels on provider mount
    useEffect(() => {
        fetchLabels();
    }, []);

    const addLabel = async (name) => {
        try {
            await labelService.createLabel(name);
            fetchLabels(); // Re-fetch labels to update UI
            return true;
        } catch (err) {
            setError(`Error adding label: ${err.message}`);
            console.error('Error adding label:', err);
            return false;
        }
    };

    const editLabel = async (id, newName) => {
        try {
            await labelService.updateLabel(id, newName);
            fetchLabels(); // Re-fetch labels
            return true;
        } catch (err) {
            setError(`Error updating label: ${err.message}`);
            console.error('Error updating label:', err);
            return false;
        }
    };

    const removeLabel = async (id) => {
        try {
            await labelService.deleteLabel(id);
            fetchLabels(); // Re-fetch labels
            return true;
        } catch (err) {
            setError(`Error deleting label: ${err.message}`);
            console.error('Error deleting label:', err);
            return false;
        }
    };

    // Make getMailsByLabel available if other components need to trigger it
    const fetchMailsForLabel = async (id) => {
        try {
            const mails = await labelService.getMailsByLabel(id);
            console.log('Mails for label:', mails);
            return mails; // Return mails for potential use in consuming components
        } catch (err) {
            setError(`Error fetching mails by label: ${err.message}`);
            console.error('Error fetching mails by label:', err);
            return null;
        }
    };

    const deleteMailFromLabel = async (labelId, mailId) => {
        try {
            await labelService.removeMailFromLabel(labelId, mailId);
            fetchLabels(); // Re-fetch labels to update UI
            return true;
        } catch (err) {
            setError(`Error removing mail from label: ${err.message}`);
            console.error('Error removing mail from label:', err);
            return false;
        }
    }

    const addMailToLabel = async (labelId, mailId) => { 
        try {
            await labelService.addMailToLabel(labelId, mailId);
            fetchLabels(); // Re-fetch labels to update UI
            return true;
        } catch (err) {
            setError(`Error adding mail to label: ${err.message}`);
            console.error('Error adding mail to label:', err);
            return false;
        }
    }

    return (
        <LabelContext.Provider
            value={{
                labels,
                loading,
                error,
                fetchLabels, // Expose fetchLabels if needed manually
                addLabel,
                editLabel,
                deleteLabel: removeLabel, // Rename to avoid conflict if needed, or just use deleteLabel
                fetchMailsForLabel,
                deleteMailFromLabel,
                addMailToLabel
            }}
        >
            {children}
        </LabelContext.Provider>
    );
};

// Custom hook to consume the LabelContext
export const useLabels = () => {
    const context = useContext(LabelContext);
    if (!context) {
        throw new Error('useLabels must be used within a LabelProvider');
    }
    return context;
};