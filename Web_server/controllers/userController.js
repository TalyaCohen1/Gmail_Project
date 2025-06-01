const userModel = require('../models/userModel');
const { isValidGmail, isValidDate, isValidGender } = require('../models/validator');

//Post /api/users
const register = (req, res) => {
  const { fullName, emailAddress, birthDate, gender, password } = req.body;

  // Validate input
   if (!fullName || !password || !fullName || !emailAddress || !birthDate || !gender) {
    return res.status(400).json({ error: "Missing required fields" });
  }
   if (!isValidGmail(emailAddress)) {
    return res.status(400).json({ error: "Email must be a valid @gmail.com address" });
  }

  if (!isValidDate(birthDate)) {
    return res.status(400).json({ error: "Invalid birthDate format. Use YYYY-MM-DD" });
  }

  if (!isValidGender(gender)) {
    return res.status(400).json({ error: "Gender must be either 'male' or 'female" });
  }
  // Check if username already exists

  if (userModel.findByEmail(emailAddress))
    return res.status(409).json({ error: 'This email adress already exists' });

  const id = Date.now().toString();
  const newUser = userModel.createUser(fullName, id, emailAddress, birthDate,gender, password);

  res.status(201).location(`/api/users/${id}`).end();
};

//Post /api/users/login
const login = (req, res) => {
  const { emailAddress, password } = req.body;

  const user = userModel.findByEmail(emailAddress);

  if (!user || user.password !== password)
    return res.status(400).json({ error: 'wrong password' });

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

  res.status(200).json({ id: user.id, fullName: user.fullName,emailAddress: user.emailAddress,birthDate: user.birthDate, gender: user.gender });
};

//Get /api/users/:id
const getUserById = (req, res) => {
  const user = userModel.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });

  res.status(200).json({
    id: user.id,
    fullName: user.fullName,
    emailAddress: user.emailAddress,
    birthDate: user.birthDate,
    gender: user.gender
  });
};

//for checking
//GET /api/users
const getAllUsers = (req, res) => {
  const users = userModel.getAllUsers();
  res.status(200).json(users);
};


module.exports = {
  register,
  login,
  getUser,
  getUserById,
  getAllUsers
};