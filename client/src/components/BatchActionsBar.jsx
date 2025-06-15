import React, { useState } from 'react';
import { removeLabelFromEmail, addLabelToEmail } from '../services/mailService'; 
// import { addMailToLabel, deleteMailFromLabel } from '../services/labelService';
import { useLabels } from '../context/LabelContext';
import '../styles/BatchActionsBar.css';


export default function BatchActionsBar({ selectedIds = [], onAction }) {
    const [selectedLabel, setSelectedLabel] = useState('');
    const { labels, addMailToLabel, deleteMailFromLabel } = useLabels();


    const handleDeleteAll = async () => {
        onAction({ type: 'delete' });
    };

    const handleApplyLabel = async () => {
        if (!selectedLabel) return;
        await Promise.all(
            selectedIds.map(id => addLabelToEmail(id, selectedLabel))
        );
        await Promise.all(
            selectedIds.map(id => addMailToLabel(selectedLabel, id))
        );
        setSelectedLabel('');
    };

    const handleRemoveLabel = async () => {
        if (!selectedLabel) return;
        await Promise.all(
            selectedIds.map(id => removeLabelFromEmail(id, selectedLabel))
        );
        await Promise.all(
        selectedIds.map(id => deleteMailFromLabel(selectedLabel, id))
        );
        setSelectedLabel('');
    };

    return (
        <div className="batch-bar">
            <span>{selectedIds.length} selected</span>
            <button onClick={handleDeleteAll}>
                Delete
            </button>

            <select
                value={selectedLabel}
                onChange={e => setSelectedLabel(e.target.value)}
            >
                <option value="">Add To Label...</option>
                {labels.map(l => (
                    <option key={l.id} value={l.id}>{l.name}</option>
                ))}
            </select>
            
            <button
                disabled={!selectedLabel}
                onClick={handleApplyLabel}
            >
                Apply Label
            </button>
        </div>
    );
}