const usersModel = require('../models/userModels');

const generateToken = (req, res) => {
  const { username, password } = req.body;
  const user = usersModel.findByUsername(username);

  if (!user || user.password !== password) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  res.status(200).json({ id: user.id });
};

module.exports = { generateToken };
