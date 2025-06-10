const { findById } = require('../models/userModel');

/**
 * Authentication middleware.
 * Checks for user ID in Authorization header and validates that the user exists.
 *
 * @param {Request} req - Express request object.
 * @param {Response} res - Express response object.
 * @param {Function} next - Callback to pass control to the next middleware.
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

const jwt = require('jsonwebtoken');
const SECRET_KEY = process.env.JWT_SECRET || 'my_super_secret_key';

function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization']; // Get the Authorization header
    const token = authHeader && authHeader.split(' ')[1]; // "Bearer <token>"

    if (!token) {
        return res.status(401).json({ error: 'Token is required' });
    }

    try{
        const decoded = jwt.verify(token, SECRET_KEY); // Verify the token using the secret key
        req.user = decoded; // Attach the decoded user information to the request object
        next(); // Call the next middleware or route handler
    } catch (err) {
        return res.status(401).json({ error: 'Invalid token' });
    }
}

module.exports = { authenticateUser, authenticateToken }; // Export the middleware functions