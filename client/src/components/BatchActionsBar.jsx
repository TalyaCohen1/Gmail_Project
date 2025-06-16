import React, { useState } from 'react';
import { removeLabelFromEmail, addLabelToEmail, deleteEmail } from '../services/mailService'; 
// import { addMailToLabel, deleteMailFromLabel } from '../services/labelService';
import { useLabels } from '../context/LabelContext';
import '../styles/BatchActionsBar.css';


export default function BatchActionsBar({ selectedIds = [], onRefresh, onAction }) {
    const [selectedLabel, setSelectedLabel] = useState('');
    const { labels, addMailToLabel, deleteMailFromLabel } = useLabels();
    const [loading, setLoading]             = useState(false);
    const [error, setError]                 = useState(null);

    const handleDeleteAll = async () => {
        setError(null);
        setLoading(true);
        try {
            await Promise.all(selectedIds.map(id => deleteEmail(id)));
            await onRefresh();
            onAction({ type: 'delete' });
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleApplyLabel = async () => {
        if (!selectedLabel) return;
        setError(null);
        setLoading(true);
        try {
            await Promise.all(
                selectedIds.map(id => addLabelToEmail(id, selectedLabel))    
            );
            await Promise.all(
                selectedIds.map(id => addMailToLabel(selectedLabel, id))
            );
            await onRefresh();
            onAction({ type: 'addLabel', labelId: selectedLabel });
            setSelectedLabel('');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveLabel = async () => {
        if (!selectedLabel) return;
        setError(null);
        setLoading(true);
        try {
            await Promise.all(
                selectedIds.map(id => removeLabelFromEmail(id, selectedLabel))
            );
            await Promise.all(
                selectedIds.map(id => deleteMailFromLabel(selectedLabel, id))
            );
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
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
                disabled={loading || !selectedLabel}                
                onClick={handleApplyLabel}
            >
                {loading ? 'Applying…' : 'Apply Label'}
            </button>
        </div>
    );
}