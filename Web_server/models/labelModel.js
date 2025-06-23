//const { addMailToLabel, removeMailFromLabel, getMailsByLabel} = require("../controllers/labelController");
const Mail = require('./mailModel'); // Assuming you have a mail model to fetch mails by IDs

let idCounter = 0;
const labels = []

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
 * Creates a set of default labels for a user if they don't already exist.
 * This function should be called when a user is first created or initialized.
 * @param {string|number} userId - The ID of the user.
 * @returns {Array<Object>} An array of the created or existing default label objects.
 */
const createDefaultLabels = (userId) => {
    const defaultLabelNames = ['Social', 'Updates', 'Forums', 'Promotions'];
    const createdOrExistingLabels = [];

    defaultLabelNames.forEach(name => {
        // Check if a label with this name already exists for the user
        const existingLabel = labels.find(label => label.name === name && label.userId === userId);
        if (!existingLabel) {
            // If not, create it
            createdOrExistingLabels.push(createLabel(name, userId));
        } else {
            // Otherwise, add the existing one to the list
            createdOrExistingLabels.push(existingLabel);
        }
    });
    return createdOrExistingLabels;
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

/** * Adds a mail to a label for a given user.
 * @param {number} labelId - The ID of the label.
 * @param {string|number} mailId - The ID of the mail.
 * @param {string|number} userId - The ID of the user.
 * @param {string} userEmail - The email of the user (to ensure mail exists for this user).
 * @returns {Object|null} The updated label or null if not found or mail does not exist.
 */
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

/** * Removes a mail from a label for a given user.
 * @param {number} labelId - The ID of the label.
 * @param {string|number} mailId - The ID of the mail.
 * @param {string|number} userId - The ID of the user.
 * @returns {Object|null} The updated label or null if not found.
 */
const removeMailFromLabel = (labelId, mailId, userId) => {
    const label = getLabel(labelId, userId);
    if (!label || !label.mails) return null;
    label.mails = label.mails.filter(id => id !== mailId);
    return label;
};

/** * Retrieves all mails associated with a specific label for a given user.
 * @param {number} labelId - The ID of the label.
 * @param {string|number} userId - The ID of the user.
 * @param {string} userEmail - The email of the user (to ensure mails belong to this user).
 * @returns {Array<Object>} An array of mail objects associated with the label.
 */
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
    getMailsByLabel,
    createDefaultLabels
};
