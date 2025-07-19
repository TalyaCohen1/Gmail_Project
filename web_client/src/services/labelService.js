// src/services/labelService.js

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

const BASE_URL = 'http://localhost:3000/api/labels';

// Helper to handle response parsing
const handleResponse = async (response) => {
    if (!response.ok) {
        let errorBody;
        try {
            // Try to parse error message from response body if available and JSON
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                errorBody = await response.json();
            } else {
                errorBody = await response.text(); // Get as text if not JSON
            }
        } catch (e) {
            // If parsing fails, use a generic error message
            errorBody = "Failed to parse error response.";
        }
        throw new Error(`HTTP error! Status: ${response.status} - ${errorBody.message || errorBody}`);
    }

    const contentType = response.headers.get("content-type");
    // Only attempt to parse as JSON if the content type is JSON and status is not 204 (No Content)
    if (response.status !== 204 && contentType && contentType.includes("application/json")) {
        return response.json();
    }
    // Return null or empty object if no content or not JSON, but success status
    return null;
};


export const getLabels = async () => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(BASE_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    return handleResponse(response); // Use the new handler
};

export const createLabel = async (name) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ name }),
    });

    return handleResponse(response); // Use the new handler
};

export const updateLabel = async (id, newName) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ name: newName }),
    });

    return handleResponse(response); // Use the new handler
};

export const deleteLabel = async (id) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    return handleResponse(response); // Use the new handler. This will return null if 204 No Content.
};

export const getMailsByLabel = async (id) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(`${BASE_URL}/${id}/mails`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    return handleResponse(response); // Use the new handler
};

export const addMailToLabel = async (labelId, mailId) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(`${BASE_URL}/${labelId}/mails`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ mailId }),
    });

    return handleResponse(response); // Use the new handler
}

export const removeMailFromLabel = async (labelId, mailId) => {
    const token = localStorage.getItem('token');
    if (!token) { throw new Error('No authentication token found'); }
    const userId = getUserIdFromToken(token);
    if (!userId) { throw new Error('Invalid token format'); }

    const response = await fetch(`${BASE_URL}/${labelId}/mails`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ mailId }),
    });

    return handleResponse(response); // Use the new handler
}
