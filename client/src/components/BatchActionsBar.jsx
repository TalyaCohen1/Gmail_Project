import React, { useState } from 'react';
import '../style/BatchActionsBar.css';

export default function BatchActionsBar({ selectedCount, labels, onAction }) {
    const [selectedLabel, setSelectedLabel] = useState('');

    return (
        <div className="batch-bar">
        <span>{selectedCount} selected</span>
        <button onClick={() => onAction({ type: 'delete' })}>Delete</button>

        <select
            value={selectedLabel}
            onChange={e => setSelectedLabel(e.target.value)}
        >
            <option value="">Add label...</option>
            {labels.map(l => (
            <option key={l.id} value={l.id}>{l.name}</option>
            ))}
        </select>
        <button
            disabled={!selectedLabel}
            onClick={() => onAction({ type: 'addLabel', labelId: selectedLabel })}
        >
            Apply Label
        </button>
        </div>
    );
}