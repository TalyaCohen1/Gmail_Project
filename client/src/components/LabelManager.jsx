// src/components/LabelManager.jsx

import React, { useState, useEffect, useRef } from 'react';
import '../styles/LabelManager.css'; // design file
import { useLabels } from '../context/LabelContext'; // Import useLabels hook

// Reusable Modal Component (remains the same)
const LabelModal = ({ show, onClose, onSubmit, initialValue = '', title }) => {
    const [value, setValue] = useState(initialValue);

    useEffect(() => {
        setValue(initialValue);
    }, [initialValue]);

    if (!show) {
        return null;
    }

    const handleSubmit = () => {
        onSubmit(value);
        setValue(''); // Clear input after submission
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h3>{title}</h3>
                <input
                    type="text"
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    placeholder="Enter label name"
                />
                <div className="modal-actions">
                    <button onClick={handleSubmit}>Save</button>
                    <button onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

const LabelManager = () => {
    const { labels, loading, error, addLabel, editLabel, deleteLabel, fetchMailsForLabel } = useLabels(); // Use the hook to get state and actions

    const [showAddLabelModal, setShowAddLabelModal] = useState(false);
    const [showEditLabelModal, setShowEditLabelModal] = useState(false);
    const [editingLabel, setEditingLabel] = useState(null); // Stores the label being edited
    const [activeLabelMenuId, setActiveLabelMenuId] = useState(null); // State to control which label's menu is open

    // Ref to detect clicks outside the menu
    const menuRef = useRef(null);

    // Close menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setActiveLabelMenuId(null);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [menuRef]);

    const handleAddLabel = async (name) => {
        if (name.trim() === '') return;
        const success = await addLabel(name);
        if (success) {
            setShowAddLabelModal(false);
        }
        // Error handling is managed by the context
    };

    const handleDeleteLabel = async (id) => {
        await deleteLabel(id);
        setActiveLabelMenuId(null); // Close menu after action
    };

    const startEditing = (label) => {
        setEditingLabel(label);
        setShowEditLabelModal(true);
        setActiveLabelMenuId(null); // Close menu after action
    };

    const handleSaveEdit = async (id, newName) => {
        const success = await editLabel(id, newName);
        if (success) {
            setShowEditLabelModal(false);
            setEditingLabel(null);
        }
        // Error handling is managed by the context
    };

    const showMailsByLabel = async (id) => {
        const mails = await fetchMailsForLabel(id);
        // Here you can handle the mails as needed, e.g., display them in a modal or another component
        // console.log('Mails for label:', mails);
    };

    const toggleMenu = (id) => {
        setActiveLabelMenuId(activeLabelMenuId === id ? null : id);
    };

    return (
        <div className="label-manager">
            <div className="label-header">
                <h2>Labels</h2>
                <button className="add-label-button" onClick={() => setShowAddLabelModal(true)} title="Add label">+</button>
            </div>

            {error && (
                <div className="error-message" style={{color: 'red', padding: '10px'}}>
                    {error}
                </div>
            )}

            {loading ? (
                <div>Loading labels...</div>
            ) : (
                <ul className="label-list">
                    {labels.map((label) => (
                        <li key={label.id}>
                            <span className="label-name-clickable" onClick={() => showMailsByLabel(label.id)}>
                                {label.name}
                            </span>
                            <div className="label-actions-container">
                                <button className="three-dots-button" onClick={() => toggleMenu(label.id)}>
                                    &#x22EF; {/* Unicode for horizontal ellipsis */}
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
                    onSubmit={(newName) => handleSaveEdit(editingLabel.id, newName)}
                    initialValue={editingLabel.name}
                    title="Edit Label"
                />
            )}
        </div>
    );
};

export default LabelManager;