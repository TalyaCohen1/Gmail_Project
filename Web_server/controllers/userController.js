const userModel = require('../models/userModel');
const { isValidGmail, isValidDate, isValidGender } = require('../models/validator');

//Post /api/users
const register = (req, res) => {
  const { fullName, emailAdress, birthDate, gender, password } = req.body;

  // Validate input
   if (!fullName || !password || !fullName || !emailAdress || !birthDate || !gender) {
    return res.status(400).json({ error: "Missing required fields" });
  }
   if (!isValidGmail(emailAdress)) {
    return res.status(400).json({ error: "Email must be a valid @gmail.com address" });
  }

  if (!isValidDate(birthDate)) {
    return res.status(400).json({ error: "Invalid birthDate format. Use YYYY-MM-DD" });
  }

  if (!isValidGender(gender)) {
    return res.status(400).json({ error: "Gender must be either 'male' or 'female" });
  }
  // Check if username already exists
  if (usersModel.findByEmail(emailAdress))
    return res.status(409).json({ error: 'This email adress already exists' });

  const id = Date.now().toString();
  const newUser = usersModel.createUser(fullName, id, emailAdress, birthDate,gender, password);

  res.status(201).location(`/api/users/${id}`).end();
};

//Post /api/users/login
const login = (req, res) => {
  const { username, password } = req.body;

  const user = usersModel.findByEmail(emailAdress);
  if (!user || user.password !== password)
    return res.status(400).json({ error: 'Invalid credentials' });

  res.status(200).json({ token: user.id });
};

//Get /api/users/me
const getUser = (req, res) => {
    const token = req.header('X-User-Id');

  if (!token)
    return res.status(401).json({ error: 'Missing token' });

  const user = usersModel.findById(token);
  if (!user)
    return res.status(404).json({ error: 'User not found' });

  res.status(200).json({ id: user.id, fullName: user.fullName,emailAdress: user.emailAdress,birthDate: user.birthDate, gender: user.gender });
};

//Get /api/users/:id
const getUserById = (req, res) => {
  const user = usersModel.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });

  res.status(200).json({
    id: user.id,
    fullName: user.fullName,
    emailAdress: user.emailAdress,
    birthDate: user.birthDate,
    gender: user.gender
  });
};


module.exports = {
  register,
  login,
  getUser,
  getUserById
};