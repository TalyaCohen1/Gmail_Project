const Label = require('../models/labelModel');
const userModel = require('../models/userModel');

/**
 * Get all labels for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getAllLabels = async (req, res) => {
    try {
        const labels = await Label.getAllLabels(req.userId);
        // Map labels to include an 'id' property for frontend compatibility
        const formattedLabels = labels.map(label => ({
            _id: label._id,
            id: label._id, // Add 'id' property
            name: label.name,
            userId: label.userId,
            mails: label.mails,
            createdAt: label.createdAt,
            updatedAt: label.updatedAt
        }));
        res.json(formattedLabels);
    } catch (error) {
        console.error('Error getting all labels:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Get a label by ID for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getLabelById = async (req, res) => {
    const id = req.params.id;
    try {
        const label = await Label.getLabel(id, req.userId);
        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }
        // Include 'id' property for frontend compatibility
        res.json({
            _id: label._id,
            id: label._id, // Add 'id' property
            name: label.name,
            userId: label.userId,
            mails: label.mails,
            createdAt: label.createdAt,
            updatedAt: label.updatedAt
        });
    } catch (error) {
        console.error('Error getting label by ID:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Create a new label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.createLabel = async (req, res) => {
    if (!req.body || typeof req.body !== 'object') {
        return res.status(400).json({ error: 'Invalid or missing request body' });
    }

    const { name } = req.body;

    if (!name || typeof name !== 'string') {
        return res.status(400).json({ error: 'Name is required and must be a string' });
    }

    try {
        const newLabel = await Label.createLabel(name, req.userId);
        if (!newLabel) {
            return res.status(500).json({ error: 'Failed to create label' });
        }
        // Respond with the full label object, including 'id' for frontend compatibility
        res.status(201)
            .location(`/api/labels/${newLabel._id}`)
            .json({
                _id: newLabel._id,
                id: newLabel._id, // Add 'id' property
                name: newLabel.name,
                userId: newLabel.userId,
                mails: newLabel.mails,
                createdAt: newLabel.createdAt,
                updatedAt: newLabel.updatedAt
            });
    } catch (error) {
        console.error('Error creating label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};


/**
 * Update an existing label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.updateLabel = async (req, res) => {
    const id = req.params.id;
    try {
        const label = await Label.getLabel(id, req.userId);

        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }
        const { name } = req.body;
        if (!name) {
            return res.status(400).json({ error: 'Name is required' });
        }
        const updatedLabel = await Label.updateLabel(id, name, req.userId);
        if (!updatedLabel) {
            return res.status(404).json({ error: 'Label not found or failed to update' });
        }
        // Include 'id' property for frontend compatibility
        res.json({
            _id: updatedLabel._id,
            id: updatedLabel._id, // Add 'id' property
            name: updatedLabel.name,
            userId: updatedLabel.userId,
            mails: updatedLabel.mails,
            createdAt: updatedLabel.createdAt,
            updatedAt: updatedLabel.updatedAt
        });
    } catch (error) {
        console.error('Error updating label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Delete a label by ID for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.deleteLabel = async (req, res) => {
    const id = req.params.id;
    try {
        const label = await Label.getLabel(id, req.userId);

        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }
        await Label.deleteLabel(id, req.userId);
        res.status(204).end();
    } catch (error) {
        console.error('Error deleting label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};


exports.addMailToLabel = async (req, res) => {
    const id = req.params.id; // Label ID from URL
    try {
        const label = await Label.getLabel(id, req.userId);

        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }
        const { mailId } = req.body;
        if (!mailId) {
            return res.status(400).json({ error: 'Mail ID is required' });
        }

        const user = await userModel.findById(req.userId);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }
        const userEmail = user.emailAddress;

        const updatedLabel = await Label.addMailToLabel(id, mailId, req.userId, userEmail);

        if (!updatedLabel) {
            return res.status(404).json({ error: 'Mail not found for this user or label not updated' });
        }
        // Respond with the updated label, including 'id' for frontend compatibility
        res.status(200).json({
            _id: updatedLabel._id,
            id: updatedLabel._id,
            name: updatedLabel.name,
            userId: updatedLabel.userId,
            mails: updatedLabel.mails,
            createdAt: updatedLabel.createdAt,
            updatedAt: updatedLabel.updatedAt
        });
    } catch (error) {
        console.error('Error adding mail to label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

exports.removeMailFromLabel = async (req, res) => {
    const id = req.params.id; // Label ID from URL
    try {
        const label = await Label.getLabel(id, req.userId);

        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }
        const { mailId } = req.body;
        if (!mailId) {
            return res.status(400).json({ error: 'Mail Id is required' });
        }
        // removeMailFromLabel returns the updated label, or null.
        const updatedLabel = await Label.removeMailFromLabel(id, mailId, req.userId);
        if (!updatedLabel) {
            return res.status(404).json({ error: 'Mail not found in label or label not found' });
        }
        // Respond with the updated label, including 'id' for frontend compatibility
        res.status(200).json({
            _id: updatedLabel._id,
            id: updatedLabel._id,
            name: updatedLabel.name,
            userId: updatedLabel.userId,
            mails: updatedLabel.mails,
            createdAt: updatedLabel.createdAt,
            updatedAt: updatedLabel.updatedAt
        });
    } catch (error) {
        console.error('Error removing mail from label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Get all mails associated with a label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getMailsByLabel = async (req, res) => {
    const id = req.params.id; // Label ID from URL
    try {
        const label = await Label.getLabel(id, req.userId);

        if (!label) {
            return res.status(404).json({ error: 'Label not found' });
        }

        const user = await userModel.findById(req.userId);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }
        const userEmail = user.emailAddress;

        const mails = await Label.getMailsByLabel(id, req.userId, userEmail);
        // Map mails to include 'id' property for frontend compatibility
        const formattedMails = mails.map(mail => ({
            ...mail.toObject(), // Convert Mongoose document to plain object
            id: mail._id // Add 'id' property
        }));
        res.json(formattedMails);
    } catch (error) {
        console.error('Error getting mails by label:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
