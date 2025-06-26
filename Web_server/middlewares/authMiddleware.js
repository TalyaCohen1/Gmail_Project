const jwt = require('jsonwebtoken');
// No longer need to import userModel here, as the token contains the necessary user ID

// Ensure that the SECRET_KEY is set in your environment variables
const SECRET_KEY = process.env.JWT_SECRET || 'my_super_secret_key';

/**
 * Middleware to authenticate requests using JWT.
 * It expects a token in the 'Authorization' header in the format 'Bearer <token>'.
 * If the token is valid, it decodes it and attaches the user's ID (from the token payload)
 * to `req.userId` for subsequent use in controllers.
 *
 * @param {Object} req - Express request object.
 * @param {Object} res - Express response object.
 * @param {Function} next - Express next middleware function.
 */
function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization']; // Get the Authorization header
    const token = authHeader && authHeader.split(' ')[1]; // "Bearer <token>"

    if (!token) {
        return res.status(401).json({ error: 'Authentication token required' });
    }

    try {
        const decoded = jwt.verify(token, SECRET_KEY); // Verify the token using the secret key
        req.user = decoded; // Attach the decoded user information to the request object
        req.userId = decoded.id; // Store user ID for later use (this is the MongoDB _id)
        next(); // Call the next middleware or route handler
    } catch (err) {
        console.error('JWT verification failed:', err.message);
        // Token is invalid or expired
        return res.status(403).json({ error: 'Invalid or expired token' });
    }
}

module.exports = authenticateToken; // Only export the authenticateToken function
