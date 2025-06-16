//const { addMailToLabel, removeMailFromLabel, getMailsByLabel} = require("../controllers/labelController");
const Mail = require('./mailModel'); // Assuming you have a mail model to fetch mails by IDs

let idCounter = 0;
const labels = [];

/**
 * Retrieves all labels belonging to a specific user.
 * @param {string|number} userId - The ID of the user.
 * @returns {Array<Object>} Array of label objects for the user.
 */
const getAllLabels = (userId) =>
    labels.filter(label => label.userId === userId);

/**
 * Retrieves a specific label by ID for a given user.
 * @param {number} id - The label ID.
 * @param {string|number} userId - The ID of the user.
 * @returns {Object|null} The label object or null if not found.
 */
const getLabel = (id, userId) =>
    labels.find(label => label.id === id && label.userId === userId);

/**
 * Creates a new label for a user.
 * @param {string} name - The name of the label.
 * @param {string|number} userId - The ID of the user.
 * @returns {Object} The newly created label object.
 */
const createLabel = (name, userId) => {
    const newLabel = { id: ++idCounter, name, userId, mails: [] };
    labels.push(newLabel);
    return newLabel;
};

/**
 * Updates the name of a label for a given user.
 * @param {number} id - The label ID.
 * @param {string} name - The new name for the label.
 * @param {string|number} userId - The ID of the user.
 * @returns {Object|null} The updated label or null if not found.
 */
const updateLabel = (id, name, userId) => {
    const label = getLabel(id, userId);
    if (!label) return null;
    if (name) label.name = name;
    if (label.name === '') label.name = 'No Name';
    return label;
};

/**
 * Deletes a label by ID for a given user.
 * @param {number} id - The label ID.
 * @param {string|number} userId - The ID of the user.
 * @returns {Object|null} The deleted label or null if not found.
 */
const deleteLabel = (id, userId) => {
    const index = labels.findIndex(label => label.id === id && label.userId === userId);
    if (index === -1) return null;
    return labels.splice(index, 1)[0];
};

const addMailToLabel = (labelId, mailId, userId, userEmail) => { // Added userEmail
    const label = getLabel(labelId, userId);
    if (!label) return null;
    if (!label.mails) label.mails = [];
    
    // Use the correct arguments (userEmail, mailId) for getById
    if (Mail.getById(userEmail, mailId) === null) {
        return null; // Ensure mail exists for this user
    }
    
    if (!label.mails.includes(mailId)) {
        label.mails.push(mailId);
    }
    return label;
};

const removeMailFromLabel = (labelId, mailId, userId) => {
    const label = getLabel(labelId, userId);
    if (!label || !label.mails) return null;
    label.mails = label.mails.filter(id => id !== mailId);
    return label;
};

const getMailsByLabel = (labelId, userId, userEmail) => { // Added userEmail
    const label = getLabel(labelId, userId);
    if (!label || !label.mails) return [];
    
    // Use the correct arguments (userEmail, mailId) here too
    const mails = label.mails.map(mailId => Mail.getById(userEmail, mailId));
    
    return mails.filter(mail => mail !== null); // Filter out any null results
};    

module.exports = {
    getAllLabels,
    getLabel,
    createLabel,
    updateLabel,
    deleteLabel,
    addMailToLabel,
    removeMailFromLabel,
    getMailsByLabel
};
