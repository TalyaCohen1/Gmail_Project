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
        'Authorization': `Bearer ${localStorage.getItem('token')}`
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
            ...getAuthHeaders()
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
            ...getAuthHeaders()
        },
        body: JSON.stringify(patchData),
    });
    if (res.status === 409) {
        throw new Error('Conflict: The email was modified by someone else.');
    }
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `Failed to update email ${id}`);
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

/**
 * Get all drafts for the authenticated user
 * @return {Promise<Array>} Array of draft emails
 */
export async function getDraftEmails() {
    const res = await fetch('http://localhost:3000/api/mails/drafts', {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch draft emails');
    }
    return res.json();
}

/**
 * Get all emails in the inbox for the authenticated user
 * @return {Promise<Array>} Array of inbox emails
 */
export async function getInboxEmails() {
    const res = await fetch('http://localhost:3000/api/mails/inbox', {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch inbox emails');
    }
    return res.json();
}

/**
 * Get all sent emails for the authenticated user
 * @return {Promise<Array>} Array of sent emails
 */
export async function getSentEmails() {
    const res = await fetch('http://localhost:3000/api/mails/sent', {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch sent emails');
    }
    return res.json();
}

/**
 * Get all spam emails for the authenticated user
 * @return {Promise<Array>} Array of spam emails
 */
export async function getSpamEmails() {
    const res = await fetch('http://localhost:3000/api/mails/spam', {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch spam emails');
    }
    return res.json();
}

/**
 * Mark an email as spam
 * @param {string|number} id of the email to mark as spam
 */
export async function markEmailAsSpam(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}/spam`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to mark email ${id} as spam`);
    }
    return res.json();
}

/**
 * Unmark an email as spam
 * @param {string|number} id of the email to unmark as spam
 */
export async function unmarkEmailAsSpam(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}/spam`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Failed to unmark email ${id} as spam`);
    }
    return res.json();
}

/**
 * Get all deleted emails for the authenticated user
 * @return {Promise<Array>} Array of deleted emails
 */
export async function getDeletedEmails() {
    const res = await fetch('http://localhost:3000/api/mails/deleted', {
        headers: getAuthHeaders(),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Failed to fetch deleted emails');
    }
    return res.json();
}