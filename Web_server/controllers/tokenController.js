const usersModel = require('../models/userModel');
const jwt = require('jsonwebtoken');
// Ensure that the SECRET_KEY is set in your environment variables
const SECRET_KEY = process.env.JWT_SECRET || 'my_super_secret_key';


/**
 * Authenticate user and issue token
 * POST /api/tokens
 */
const createToken = async (req, res) => {
  const { emailAddress, password } = req.body;

  if (!emailAddress || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  try {
    const user = await usersModel.findByEmail(emailAddress);

    if (!user) {
      return res.status(404).json({ error: 'Email address doesn\'t fit any user' });
    }

    // In a real application, you should hash and compare passwords securely (e.g., bcrypt)
    if (user.password !== password) {
      return res.status(400).json({ error: 'Wrong password!' });
    }

    const token = jwt.sign(
      { id: user._id, emailAddress: user.emailAddress },
      SECRET_KEY,
      { expiresIn: '1h' } // Token expires in 1 hour
    );

    // Explicitly add 'id' property for frontend compatibility
    res.status(200).json({
      token,
      fullName: user.fullName,
      profileImage: user.profileImage,
      userId: user._id, // Keep userId as _id
      _id: user._id, // Keep _id
      id: user._id // Add id for compatibility
    });
  } catch (error) {
    console.error("Error creating token:", error);
    res.status(500).json({ error: "Internal server error during token creation" });
  }
};

module.exports = { createToken };
