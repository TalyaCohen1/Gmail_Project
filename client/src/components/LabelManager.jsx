// src/components/LabelManager.jsx

import React, { useState, useEffect, useRef } from 'react';
import LabelModal from './LabelModal';
import '../styles/LabelManager.css';
import { useLabels } from '../context/LabelContext';

// Define default label names (must match what you use in labelModel.js)
const DEFAULT_LABEL_NAMES = ['Social', 'Updates', 'Forums', 'Promotions']; // Add any other default labels you create

// Accepts new props for managing displayed emails from Sidebar
const LabelManager = ({ setDisplayedEmails, setDisplayLoading, setDisplayError }) => {
    const { labels, loading, error, addLabel, editLabel, deleteLabel, fetchMailsForLabel } = useLabels();
    const [showAddLabelModal, setShowAddLabelModal] = useState(false);
    const [showEditLabelModal, setShowEditLabelModal] = useState(false);
    const [editingLabel, setEditingLabel] = useState(null);
    const [activeLabelMenuId, setActiveLabelMenuId] = useState(null);
    const menuRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target) && !event.target.closest('.three-dots-button')) {
                setActiveLabelMenuId(null);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [activeLabelMenuId]);


    const handleAddLabel = async (name) => {
        try {
            await addLabel(name);
            setShowAddLabelModal(false);
        } catch (err) {
            console.error("Error adding label:", err);
        }
    };

    const handleEditLabel = async (id, newName) => {
        try {
            await editLabel(id, newName);
            setShowEditLabelModal(false);
            setEditingLabel(null);
        } catch (err) {
            console.error("Error editing label:", err);
        }
    };

    const handleDeleteLabel = async (id) => {
        try {
            await deleteLabel(id);
            setActiveLabelMenuId(null);
        } catch (err) {
            console.error("Error deleting label:", err);
        }
    };

    const startEditing = (label) => {
        setEditingLabel(label);
        setShowEditLabelModal(true);
        setActiveLabelMenuId(null);
    };

    const toggleMenu = (labelId) => {
        setActiveLabelMenuId(activeLabelMenuId === labelId ? null : labelId);
    };

    // This function updates the displayed emails when a custom label is clicked
    const handleShowMailsForLabel = async (labelId) => {
        setDisplayLoading(true); // Set loading via prop
        setDisplayError(null);   // Clear previous errors via prop
        try {
            const mails = await fetchMailsForLabel(labelId); // Fetch mails for the label
            setDisplayedEmails(mails); // Update the emails displayed in EmailList via prop
            setActiveLabelMenuId(null); // Close the menu after selection
        } catch (err) {
            setDisplayError(`Error fetching mails for label: ${err.message}`); // Set error via prop
            console.error("Error fetching mails for label:", err);
            setDisplayedEmails([]); // Clear emails on error
        } finally {
            setDisplayLoading(false); // Clear loading via prop
        }
    };

    if (loading) return <div>Loading labels...</div>;
    if (error) return <div>Error: {error.message}</div>;

    // Filter out default labels before mapping
    const customLabels = labels.filter(label => !DEFAULT_LABEL_NAMES.includes(label.name));

    return (
        <div className="label-manager">
            <div className="label-manager-header">
                <h3>
                    Labels
                </h3>
                <button
                    className="add-label-button"
                    onClick={() => setShowAddLabelModal(true)}
                >
                    <img
                        src="/icons/plus.svg"
                        alt="Add Label"
                        className="button-icon"
                        onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=+" }}
                    />
                </button>
            </div>
            {customLabels.length === 0 ? (
                <p style={{ padding: '0 16px' }}>No custom labels yet.</p>
            ) : (
                <ul className="label-list">
                    {customLabels.map((label) => (
                        <li key={label.id}>
                            <img
                                src="/icons/labels.svg"
                                alt="Custom Label Icon"
                                className="button-icon"
                                onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=L" }}
                            />
                            {/* Changed span to button for better accessibility and click handling */}
                            <button onClick={() => handleShowMailsForLabel(label.id)} className="label-name-clickable">
                                <span>{label.name}</span>
                                {label.mails && label.mails.length > 0 && (
                                    <span className="mail-count">{label.mails.length}</span>
                                )}
                            </button>
                            <div className="label-actions-container">
                                <button className="three-dots-button" onClick={() => toggleMenu(label.id)}>
                                    <img
                                        src="/icons/3_dots.svg"
                                        alt="Actions"
                                        className="button-icon"
                                        onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=..." }}
                                    />
                                </button>
                                {activeLabelMenuId === label.id && (
                                    <div className="label-action-menu" ref={menuRef}>
                                        <button onClick={() => startEditing(label)}>Edit</button>
                                        <button onClick={() => handleDeleteLabel(label.id)}>Delete</button>
                                    </div>
                                )}
                            </div>
                        </li>
                    ))}
                </ul>
            )}

            <LabelModal
                show={showAddLabelModal}
                onClose={() => setShowAddLabelModal(false)}
                onSubmit={handleAddLabel}
                title="Create New Label"
            />

            {editingLabel && (
                <LabelModal
                    show={showEditLabelModal}
                    onClose={() => setShowEditLabelModal(false)}
                    onSubmit={(newName) => handleEditLabel(editingLabel.id, newName)}
                    initialValue={editingLabel.name}
                    title="Edit Label"
                />
            )}
        </div>
    );
};

export default LabelManager;