// src/components/LabelManager.jsx

import React, { useState, useEffect, useRef } from 'react';
import LabelModal from './LabelModal';
import '../styles/LabelManager.css';
import { useLabels } from '../context/LabelContext';

const LabelManager = () => {
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

    const handleShowMailsForLabel = async (labelId) => {
        try {
            const mails = await fetchMailsForLabel(labelId);
            console.log(`Mails for label ${labelId}:`, mails);
            // Implement UI update here to show mails for this label
        } catch (err) {
            console.error("Error fetching mails for label:", err);
        }
    };


    if (loading) return <div>Loading labels...</div>;
    if (error) return <div>Error: {error.message}</div>;

    return (
        <div className="label-manager">
            <div className="label-manager-header">
                <h3>
                    {/* **UPDATED: Icon for Labels header now uses labels.svg** */}
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
            {labels.length === 0 ? (
                <p style={{ padding: '0 16px' }}>No custom labels yet.</p>
            ) : (
                <ul className="label-list">
                    {labels.map((label) => (
                        <li key={label.id}>
                            <img
                                src="/icons/labels.svg"
                                alt="Custom Label Icon"
                                className="button-icon"
                                onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/20x20/cccccc/000000?text=L" }}
                            />
                            <span onClick={() => handleShowMailsForLabel(label.id)} className="label-name-clickable">
                                {label.name}
                            </span>
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