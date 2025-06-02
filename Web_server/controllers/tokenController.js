const usersModel = require('../models/userModel');

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

  res.status(200).json({ id: user.id });
};

module.exports = { createToken };
