const usersModel = require('../models/userModels');

//POST /api/tokens
const createToken = (req, res) => {
  const { username, password } = req.body;
  const user = usersModel.findByUsername(username);

  if (!user || user.password !== password) {
    return res.status(400).json({ error: 'Invalid credentials' });
  }

  res.status(200).json({ id: user.id });
};

module.exports = { createToken };
