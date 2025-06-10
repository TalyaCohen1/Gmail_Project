import React, { useState, useEffect, useRef } from 'react';
import '../styles/LabelManager.css'; // design file

// Reusable Modal Component
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
    const [labels, setLabels] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showAddLabelModal, setShowAddLabelModal] = useState(false);
    const [showEditLabelModal, setShowEditLabelModal] = useState(false);
    const [editingLabel, setEditingLabel] = useState(null); // Stores the label being edited
    const [activeLabelMenuId, setActiveLabelMenuId] = useState(null); // State to control which label's menu is open

    // Ref to detect clicks outside the menu
    const menuRef = useRef(null);

    // fetch labels when component mounts
    useEffect(() => {
        fetchLabels();
    }, []);

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

    // Helper function to decode JWT and extract user ID
    const getUserIdFromToken = (token) => {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.id;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    };

    const fetchLabels = async () => {
        setLoading(true);
        setError(null);
        try {
            // Get token from localStorage
            const token = localStorage.getItem('token');
            
            if (!token) {
                throw new Error('No authentication token found');
            }

            // Extract user ID from JWT token
            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token format');
            }

            const response = await fetch('http://localhost:3000/api/labels', {
                method: 'GET',
                headers: {
                    'Authorization': `${userId}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Ensure data is an array
            if (Array.isArray(data)) {
                setLabels(data);
            } else {
                console.error('Expected array but got:', data);
                setLabels([]);
                setError('Invalid data format received from server');
            }
        } catch (error) {
            console.error('Could not fetch labels:', error);
            setLabels([]); // Set empty array to prevent map error
            setError(`Failed to fetch labels: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    const addLabel = async (name) => {
        if (name.trim() === '') return;

        try {
            const token = localStorage.getItem('token');
            
            if (!token) {
                throw new Error('No authentication token found');
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token format');
            }

            const response = await fetch('http://localhost:3000/api/labels', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `${userId}`
                },
                body: JSON.stringify({ name }),
            });

            if (response.ok) {
                setShowAddLabelModal(false);
                fetchLabels();
            } else {
                setError(`Failed to add label: ${response.status}`);
            }
        } catch (error) {
            console.error('Error adding label:', error);
            setError(`Error adding label: ${error.message}`);
        }
    };

    const deleteLabel = async (id) => {
        try {
            const token = localStorage.getItem('token');
            
            if (!token) {
                throw new Error('No authentication token found');
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token format');
            }

            const response = await fetch(`http://localhost:3000/api/labels/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `${userId}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                fetchLabels();
            } else {
                setError(`Failed to delete label: ${response.status}`);
            }
        } catch (error) {
            console.error('Error deleting label:', error);
            setError(`Error deleting label: ${error.message}`);
        } finally {
            setActiveLabelMenuId(null); // Close menu after action
        }
    };

    const startEditing = (label) => {
        setEditingLabel(label);
        setShowEditLabelModal(true);
        setActiveLabelMenuId(null); // Close menu after action
    };

    const saveEdit = async (id, newName) => {
        try {
            const token = localStorage.getItem('token');
            
            if (!token) {
                throw new Error('No authentication token found');
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token format');
            }

            const response = await fetch(`http://localhost:3000/api/labels/${id}`, {
                method: 'PATCH',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `${userId}`
                },
                body: JSON.stringify({ name: newName }),
            });

            if (response.ok) {
                setShowEditLabelModal(false);
                setEditingLabel(null);
                fetchLabels();
            } else {
                setError(`Failed to update label: ${response.status}`);
            }
        } catch (error) {
            console.error('Error updating label:', error);
            setError(`Error updating label: ${error.message}`);
        }
    };

    const showMailsByLabel = async (id) => {
        try {
            const token = localStorage.getItem('token');
            
            if (!token) {
                throw new Error('No authentication token found');
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                throw new Error('Invalid token format');
            }

            const response = await fetch(`http://localhost:3000/api/labels/${id}/mails`, {
                method: 'GET',
                headers: {
                    'Authorization': `${userId}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                const mails = await response.json();
                console.log('Mails for label:', mails);
                // Here you can handle the mails as needed, e.g., display them in a modal or another component
            } else {
                setError(`Failed to fetch mails for label: ${response.status}`);
            }
        } catch (error) {
            console.error('Error fetching mails by label:', error);
            setError(`Error fetching mails by label: ${error.message}`);
        }
    }

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
                                        <button onClick={() => deleteLabel(label.id)}>Delete</button>
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
                onSubmit={addLabel}
                title="Create New Label"
            />

            {editingLabel && (
                <LabelModal
                    show={showEditLabelModal}
                    onClose={() => setShowEditLabelModal(false)}
                    onSubmit={(newName) => saveEdit(editingLabel.id, newName)}
                    initialValue={editingLabel.name}
                    title="Edit Label"
                />
            )}
        </div>
    );
};

export default LabelManager;