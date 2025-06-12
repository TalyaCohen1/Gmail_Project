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

export async function getEmailById(id) {
    const res = await fetch(`http://localhost:3000/api/mails/${id}`, {
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
    console.log('Creating email with data:', data);
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
 * @param {string|number} id
 * @param {Object} patchData – {subject, body, to}
 */
export async function updateEmail(id, patchData) {
    console.log('Updating email:', id);
    console.log('Patch data:', patchData);

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
        throw new Error(err.message || `Failed to update email ${id}`);
    }
    return res.json();
}

/**
 * Delete an email
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