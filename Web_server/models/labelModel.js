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
    const newLabel = { id: ++idCounter, name, userId };
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

module.exports = {
    getAllLabels,
    getLabel,
    createLabel,
    updateLabel,
    deleteLabel
};
