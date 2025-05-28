const userModel = require('../models/userModel');

//Post /api/users
const register = (req, res) => {
  const { username, password, email, avatar } = req.body;

  // Validate input
  if (!username || !password || !email || !avatar)
    return res.status(400).json({ error: 'Missing username or password' });

  // Check if username already exists
  if (userModel.findByUsername(username))
    return res.status(409).json({ error: 'Username already exists' });

  const id = Date.now().toString();
  const newUser = userModel.createUser(username, id, email, avatar, password);

  res.status(201).location(`/api/users/${id}`).end();
};

//Post /api/users/login
const login = (req, res) => {
  const { username, password } = req.body;

  const user = userModel.findByUsername(username);
  if (!user || user.password !== password)
    return res.status(400).json({ error: 'Invalid credentials' });

  res.status(200).json({ token: user.id });
};

//Get /api/users/me
const getUser = (req, res) => {
    const token = req.header('X-User-Id');

  if (!token)
    return res.status(401).json({ error: 'Missing token' });

  const user = userModel.findById(token);
  if (!user)
    return res.status(404).json({ error: 'User not found' });

  res.status(200).json({ id: user.id, username: user.username,email: user.email,avatar: user.avatar });
};

//Get /api/users/:id
const getUserById = (req, res) => {
  const user = userModel.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });

  res.status(200).json({
    id: user.id,
    username: user.username,
    email: user.email,
    avatar: user.avatar
  });
};


module.exports = {
  register,
  login,
  getUser,
  getUserById
};