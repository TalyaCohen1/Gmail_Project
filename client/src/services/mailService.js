// mailService.js
const API_BASE = process.env.REACT_APP_API_BASE_URL || '';

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }) // check if the word 'bearer' needed
    };
}

export async function getEmails() {
    const res = await fetch(`${API_BASE}/api/mails`, {
        headers: getAuthHeaders(), 
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch emails');
    }
    return res.json();
}

export async function getEmailById(id) {
    const res = await fetch(`${API_BASE}/api/mails/${id}`, {
        headers: getAuthHeaders(), 
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch email ${id}');
    }
    return res.json();
}

/**
 * Create new email
 * @param {Object} data – { to, subject, body, labels: [id,…] }
 */
export async function createEmail(data) {
    const res = await fetch(`${API_BASE}/api/mails`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to send mail');
    }
    return res.json();
}

/**
 * Update a draft email
 * @param {string|number} id
 * @param {Object} patchData – {subject, body, to}
 */
export async function updateEmail(id, patchData) {
    const res = await fetch(`${API_BASE}/api/mails/${id}`, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(patchData),
    });
    if (res.status === 409) {
        throw new Error('Conflict: The email was modified by someone else.');
    }
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to update email ${id}`);
    }
    return res.json();
}

/**
 * Delete an email
 */
export async function deleteEmail(id) {
    const res = await fetch(`${API_BASE}/api/mails/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to delete email ${id}`);
    }

    return res.json().catch(() => ({}));
}