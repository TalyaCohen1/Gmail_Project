// src/components/LabelModal.jsx

import React, { useState, useEffect } from 'react';
import '../styles/LabelManager.css'; // Make sure this path is correct relative to LabelModal.jsx

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

export default LabelModal; // Export the component