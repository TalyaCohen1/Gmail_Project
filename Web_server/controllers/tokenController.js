const usersModel = require('../models/userModel');
const jwt = require('jsonwebtoken');
// Ensure that the SECRET_KEY is set in your environment variables
const SECRET_KEY = 'my_super_secret_key';


/**
 * Authenticate user and issue token
 * POST /api/tokens
 */
const createToken = (req, res) => {
  const { emailAddress, password } = req.body;
  const user = usersModel.findByEmail(emailAddress);
  if (!emailAddress || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }
  if (!user){
      return res.status(404).json({ error: 'email Address dont fit to any user' });
  }
  if (user.password !== password) {
    return res.status(400).json({ error: 'wrong password!' });
  }

  const token = jwt.sign(
    { id: user.id, emailAddress: user.emailAddress },
    SECRET_KEY,
    { expiresIn: '1h' } // Token expires in 1 hour
  );
  res.status(200).json({token}); // Respond with the token
};

module.exports = { createToken };
