const service = require('../services/blacklistService');

/**
 * Controller to handle adding a URL to the blacklist.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.addToBlacklist = (req, res) => {
    const { url } = req.body;
    if (!url || typeof url !== 'string') {
        return res.status(400).json({ error: 'Valid URL is required in request body' });
    }

    // Proceed to service function with full req, res
    service.addUrl(req, res);
};

/**
 * Controller to handle deleting a URL from the blacklist.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.deleteFromBlacklist = (req, res) => {
    const { id } = req.params;
    if (!id || typeof id !== 'string') {
        return res.status(400).json({ error: 'Valid URL is required in request' });
    }

    service.removeUrl(req, res);
};

/**
 * Controller to handle checking if a URL is in the blacklist.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 */
exports.checkBlacklist = (req, res) => {
    const { url } = req.query;
    if (!url || typeof url !== 'string') {
        return res.status(400).json({ error: 'Valid URL is required as a query parameter' });
    }

    service.checkUrl(req, res);
};
