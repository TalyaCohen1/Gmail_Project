import React, { useState, useEffect } from 'react';
import '../styles/LabelManager.css'; // design file

const LabelManager = () => {
    const [labels, setLabels] = useState([]);
    const [newLabel, setNewLabel] = useState('');
    const [editingLabel, setEditingLabel] = useState(null);
    const [editValue, setEditValue] = useState('');
    //const [labelIdInput, setLabelIdInput] = useState('');
    const [label, setLabel] = useState(null);

    // fetch labels when component mounts
    useEffect(() => {
        fetchLabels();
    }, []);

    const fetchLabels = async () => {
        try {
            const response = await fetch('http://localhost:3000/labels', {
                method: 'GET'
            });
            const data = await response.json();
            setLabels(data);
        } catch (error) {
            console.error('Could not fetch labels:', error);
        }
    };

    const fetchLabelById = async (id) => {
        try {
            const response = await fetch(`http://localhost:3000/labels/${id}`, {
                method: 'GET',
            });
            if (response.ok) {
                const data = await response.json();
                setLabel(data);
            } else {
                setLabel(null);
            }
        } catch (error) {
            console.error('Error fetching label by ID:', error);
            setLabel(null);
        }
    };

    const addLabel = async () => {
        if (newLabel.trim() === '') return;

        try {
            const response = await fetch('http://localhost:3000/labels', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: newLabel }),
            });

            if (response.ok) {
                setNewLabel('');
                fetchLabels();
            }
        } catch (error) {
            console.error('Error adding label:', error);
        }
    };

    const deleteLabel = async (id) => {
        try {
            const response = await fetch(`http://localhost:3000/labels/${id}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                fetchLabels();
            }
        } catch (error) {
            console.error('Error deleting label:', error);
        }
    };

    const startEditing = (label) => {
        setEditingLabel(label.id);
        setEditValue(label.name);
    };

    const saveEdit = async (id) => {
        try {
            const response = await fetch(`http://localhost:3000/labels/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: editValue }),
            });

            if (response.ok) {
                setEditingLabel(null);
                setEditValue('');
                fetchLabels();
            }
        } catch (error) {
            console.error('Error updating label:', error);
        }
    }

     return (
        <div className="label-manager">
            <div className="label-header">
                <h2>Labels</h2>
                <button className="add-label-button" onClick={addLabel} title="Add label">+</button>
            </div>

            <div className="new-label">
                <input
                    type="text"
                    placeholder="Add new label"
                    value={newLabel}
                    onChange={(e) => setNewLabel(e.target.value)}
                />
            </div>

            <ul className="label-list">
                {labels.map((label) => (
                    <li key={label.id}>
                        {editingLabel === label.id ? (
                            <>
                                <input
                                    type="text"
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                />
                                <button onClick={() => saveEdit(label.id)}>Save</button>
                                <button onClick={() => setEditingLabel(null)}>Cancel</button>
                            </>
                        ) : (
                            <>
                                <span>{label.name}</span>
                                <button onClick={() => startEditing(label)}>Edit</button>
                                <button onClick={() => deleteLabel(label.id)}>Delete</button>
                            </>
                        )}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default LabelManager;
