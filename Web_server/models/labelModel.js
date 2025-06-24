const mongoose = require('mongoose');
const Mail = require('./mailModel'); // Still assuming you have a mail model to fetch mails by IDs

// Define the Label Schema
const labelSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    userId: {
        type: mongoose.Schema.Types.ObjectId, // Assuming userId will be a MongoDB ObjectId
        required: true
    },
    mails: [{
        type: String, // Assuming mailId is a string (e.g., Gmail message ID)
        ref: 'Mail' // If you want to populate mail details later, reference the Mail model
    }]
});

// Create the Label Model
const Label = mongoose.model('Label', labelSchema);

/**
 * Retrieves all labels belonging to a specific user.
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of label objects for the user.
 */
const getAllLabels = async (userId) => {
    return await Label.find({ userId }).exec();
};

/**
 * Retrieves a specific label by ID for a given user.
 * @param {string} id - The label ID (MongoDB ObjectId string).
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Object|null>} Promise resolving to the label object or null if not found.
 */
const getLabel = async (id, userId) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    return await Label.findOne({ _id: id, userId }).exec();
};

/**
 * Creates a new label for a user.
 * @param {string} name - The name of the label.
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Object>} Promise resolving to the newly created label object.
 */
const createLabel = async (name, userId) => {
    const newLabel = new Label({ name, userId, mails: [] });
    await newLabel.save();
    return newLabel;
};

/**
 * Creates a set of default labels for a user if they don't already exist.
 * This function should be called when a user is first created or initialized.
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of the created or existing default label objects.
 */
const createDefaultLabels = async (userId) => {
    const defaultLabelNames = ['Social', 'Updates', 'Forums', 'Promotions'];
    const createdOrExistingLabels = [];

    for (const name of defaultLabelNames) {
        let existingLabel = await Label.findOne({ name, userId }).exec();
        if (!existingLabel) {
            existingLabel = await createLabel(name, userId);
        }
        createdOrExistingLabels.push(existingLabel);
    }
    return createdOrExistingLabels;
};

/**
 * Updates the name of a label for a given user.
 * @param {string} id - The label ID (MongoDB ObjectId string).
 * @param {string} name - The new name for the label.
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Object|null>} Promise resolving to the updated label or null if not found.
 */
const updateLabel = async (id, name, userId) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const label = await Label.findOne({ _id: id, userId }).exec();
    if (!label) return null;

    if (name) label.name = name;
    if (label.name === '') label.name = 'No Name'; // Consider if this logic is still desired
    await label.save();
    return label;
};

/**
 * Deletes a label by ID for a given user.
 * @param {string} id - The label ID (MongoDB ObjectId string).
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Object|null>} Promise resolving to the deleted label or null if not found.
 */
const deleteLabel = async (id, userId) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const result = await Label.findOneAndDelete({ _id: id, userId }).exec();
    return result; // Returns the deleted document or null
};

/**
 * Adds a mail to a label for a given user.
 * @param {string} labelId - The ID of the label (MongoDB ObjectId string).
 * @param {string} mailId - The ID of the mail.
 * @param {string} userId - The ID of the user.
 * @param {string} userEmail - The email of the user (to ensure mail exists for this user).
 * @returns {Promise<Object|null>} Promise resolving to the updated label or null if not found or mail does not exist.
 */
const addMailToLabel = async (labelId, mailId, userId, userEmail) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(labelId)) {
        return null;
    }
    const label = await Label.findOne({ _id: labelId, userId }).exec();
    if (!label) return null;

    // Assuming Mail.getById is an async function now
    const mailExists = await Mail.getById(userEmail, mailId);
    if (!mailExists) {
        return null; // Ensure mail exists for this user
    }

    if (!label.mails.includes(mailId)) {
        label.mails.push(mailId);
        await label.save();
    }
    return label;
};

/**
 * Removes a mail from a label for a given user.
 * @param {string} labelId - The ID of the label (MongoDB ObjectId string).
 * @param {string} mailId - The ID of the mail.
 * @param {string} userId - The ID of the user.
 * @returns {Promise<Object|null>} Promise resolving to the updated label or null if not found.
 */
const removeMailFromLabel = async (labelId, mailId, userId) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(labelId)) {
        return null;
    }
    const label = await Label.findOne({ _id: labelId, userId }).exec();
    if (!label) return null;

    label.mails = label.mails.filter(id => id !== mailId);
    await label.save();
    return label;
};

/**
 * Retrieves all mails associated with a specific label for a given user.
 * @param {string} labelId - The ID of the label (MongoDB ObjectId string).
 * @param {string} userId - The ID of the user.
 * @param {string} userEmail - The email of the user (to ensure mails belong to this user).
 * @returns {Promise<Array<Object>>} Promise resolving to an array of mail objects associated with the label.
 */
const getMailsByLabel = async (labelId, userId, userEmail) => {
    // Ensure the ID is a valid MongoDB ObjectId before querying
    if (!mongoose.Types.ObjectId.isValid(labelId)) {
        return [];
    }
    const label = await Label.findOne({ _id: labelId, userId }).exec();
    if (!label || !label.mails) return [];

    // Use Promise.all to fetch mails concurrently
    const mailPromises = label.mails.map(mailId => Mail.getById(userEmail, mailId));
    const mails = await Promise.all(mailPromises);

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