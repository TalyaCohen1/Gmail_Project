
const { findById } = require('../models/userModel');

/**
 * Authentication middleware
 * Checks for user ID in Authorization header and validates user exists
 */
const authenticateUser = (req, res, next) => {
    const userId = req.headers.authorization;
    
    if (!userId) {
        return res.status(401).json({ error: 'Authorization header is required' });
    }
    
    const user = findById(userId);
    if (!user) {
        return res.status(401).json({ error: 'Invalid user ID' });
    }
    
    
    req.userId = userId;
    req.user = user;
    next();
};

module.exports = { authenticateUser };