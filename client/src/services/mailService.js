// mailService.js

// Helper function to decode JWT and extract user ID
const getUserIdFromToken = (token) => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.id;
    } catch (error) {
        console.error('Error decoding token:', error);
        return null;
    }
}

function getAuthHeaders() {
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

    return {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `${userId}` })
    };
}

export async function getEmails() {
    const res = await fetch('http://localhost:3000/api/mails', {
        headers: getAuthHeaders(), 
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch emails');
    }
    return res.json();
}

/**
 * Get a single email by ID
 * @param {string|number} id of the email
 */
export async function getEmailById(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}`, {
        headers: getAuthHeaders(), 
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to fetch email ${id}`);
    }
    return res.json();
}

/**
 * Create new email
 * @param {Object} data – { to, subject, body, labels: [id,…] }
 */
export async function createEmail(data) {
    const res = await fetch('http://localhost:3000/api/mails', {
        method: 'POST',
        headers: {
            ...getAuthHeaders(),
            'Content-Type': 'application/json'
        },
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
 * @param {string|number} id of the email
 * @param {Object} patchData – {subject, body, to}
 */
export async function updateEmail(id, patchData) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}`, {
        method: 'PATCH',
        headers: {
            ...getAuthHeaders(),
            'Content-Type': 'application/json'
        },
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
 * @param {string|number} id of the email to delete
 */
export async function deleteEmail(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to delete email ${id}`);
    }

    return res.json().catch(() => ({}));
}

/**
 * Search emails by query string in subject or body
 * @param {string} query
 */
export async function searchEmails(query) {
    const res = await fetch(`http://localhost:3000/api/mails/search/${encodeURIComponent(query)}`, {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to search emails');
    }
    return res.json();
}

/**
 * Get all labels for a mail
 * @param {string|number} id of the email
 */
export async function getEmailLabels(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}/labels`, {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to fetch labels for email ${id}`);
    }
    return res.json();
}

/**
 * Add a label to an email
 * @param {string|number} id of the email
 * @param {string|number} labelId of the label to add
 */
export async function addLabelToEmail(id, labelId) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}/labels`, {
        method: 'POST',
        headers: {
            ...getAuthHeaders(),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ labelId }),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to add label to email ${id}`);
    }
    return res.json();
}

/**
 * Remove a label from an email
 * @param {string|number} id of the email
 * @param {string|number} labelId of the label to remove
 */
export async function removeLabelFromEmail(id, labelId) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}/labels/${labelId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to remove label from email ${id}`);
    }
    return res.json();
}
