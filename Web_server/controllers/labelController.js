const Label = require('../models/labelModel');

/**
 * Get all labels for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getAllLabels = (req, res) => {
    res.json(Label.getAllLabels(req.userId));
};

/**
 * Get a label by ID for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getLabelById = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);
    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    res.json(label);
};

/**
 * Create a new label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.createLabel = (req, res) => {
    if (!req.body || typeof req.body !== 'object') {
        return res.status(400).json({ error: 'Invalid or missing request body' });
    }

    const { name } = req.body;

    if (!name || typeof name !== 'string') {
        return res.status(400).json({ error: 'Name is required and must be a string' });
    }

    const newLabel = Label.createLabel(name, req.userId);
    if (!newLabel) {
        return res.status(500).json({ error: 'Failed to create label' });
    }

    res.status(201).location(`/api/labels/${newLabel.id}`).end();
};


/**
 * Update an existing label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.updateLabel = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);

    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    const { name } = req.body;
    if (!name) {
        return res.status(400).json({ error: 'Name is required' });
    }
    const updatedLabel = Label.updateLabel(label.id, name, req.userId);
    if (!updatedLabel) {
        return res.status(404).json({ error: 'Label not found' });
    }
    res.json(updatedLabel);
};

/**
 * Delete a label by ID for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.deleteLabel = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);
    
    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    Label.deleteLabel(label.id, req.userId);
    res.status(204).end();
};


exports.addMailToLabel = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);
    
    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    const { mailId } = req.body;
    if (!mailId) {
        return res.status(400).json({ error: 'Mail ID is required and must be a string' });
    }
    Label.addMailToLabel(label.id, mailId, req.userId);
    res.status(204).end();
};

exports.removeMailFromLabel = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);
    
    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    const { mailId } = req.body;
    if (!mailId) {
        return res.status(400).json({ error: 'Mail Id is required and must be a string' });
    }
    Label.removeMailFromLabel(label.id, mailId, req.userId);
    res.status(204).end();
}

/**
 * Get all mails associated with a label for the authenticated user.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.getMailsByLabel = (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid label ID' });
    }
    const label = Label.getLabel(parseInt(req.params.id), req.userId);
    
    if (!label) {
        return res.status(404).json({ error: 'Label not found' });
    }
    const mails = Label.getMailsByLabel(label.id, req.userId);
    res.json(mails);
}
