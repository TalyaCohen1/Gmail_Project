const userModel = require('../models/userModel');
const { isValidGmail, isValidDateFormat,isPastDate, isAgeOver13, isValidGender, isValidPassword } = require('../models/validator');

/**
 * register a new user
 * POST /api/users
 * @param {*} req - The request object containing user data
 * @param {*} res  - The response object to send back the result 
 */
const register = (req, res) => {
  const { fullName, emailAddress, birthDate, gender, password} = req.body;
  const profileImage = req.file
    ? `/uploads/${req.file.filename}`
    : '/uploads/default-profile.png';

  // Validate input
   if (!fullName || !password|| !emailAddress || !birthDate || !gender) {
    return res.status(400).json({ error: "Missing required fields" });
  }
   if (!isValidGmail(emailAddress)) {
    return res.status(400).json({ error: "Email must be a valid @gmail.com address" });
  }

  if (!isValidDateFormat(birthDate)) {
    return res.status(400).json({ error: "Birth date must be in the format YYYY-MM-DD" });
  }
  if (!isPastDate(birthDate)) {
    return res.status(400).json({ error: "Birth date must be in the past" });
  }
  if (!isAgeOver13(birthDate)) {
    return res.status(400).json({ error: "User must be over 13 years old" });
  }
  if (!isValidPassword(password)) {
    return res.status(400).json({ error: "Password must be at least 8 characters long, contain at least one digit and one uppercase letter" });
  }

  if (!isValidGender(gender)) {
    return res.status(400).json({ error: "Gender must be either 'male' or 'female" });
  }
  // Check if emailAddress already exists
  if (userModel.findByEmail(emailAddress))
    return res.status(400).json({ error: 'This email adress already exists' });

  const id = Date.now().toString(); // Generate a unique ID based on the current timestamp
  const newUser = userModel.createUser(fullName, id, emailAddress, birthDate,gender, password , profileImage);

  //response with the new user id 
    res.status(201)
      .location(`/api/users/${id}`)
      .json({ id, fullName, profileImage });
};

/**
 * Login a user
 * Post /api/users/login
 * @param {*} req - The request object containing email and password
 * @param {*} res - The response object to send back the result
 */
const login = (req, res) => {
  const { emailAddress, password } = req.body;

  const user = userModel.findByEmail(emailAddress);

  if (!user || user.password !== password)
    return res.status(400).json({ error: 'wrong password' });

  res.status(200).json({ token: user.id , fullName: user.fullName, profileImage: user.profileImage || '/uploads/default-profile.png' });
};

/**
 * Get user by ID from URL
 * GET /api/users/:id
 * @param {*} req - request object
 * @param {*} res - response object
 */
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

/**
 * Get all users (for admin/testing)
 * GET /api/users
 * @param {*} req - request object
 * @param {*} res - response object
 */
const getAllUsers = (req, res) => {
  const users = userModel.getAllUsers();
  res.status(200).json(users);
};

// Update user information
const updateUser = (req, res) => {
  const userId = req.params.id;
  const { fullName } = req.body;

  const users = userModel.getAllUsers();
  const userToUpdate = userModel.findById(userId);
  if (!userToUpdate) {
    return res.status(404).json({ error: 'User not found' });
  }
  // Update the user information
  if (fullName) {
    userToUpdate.fullName = fullName;
  }
  if (req.file) {
    userToUpdate.profileImage = `/uploads/${req.file.filename}`;
  }
}

// Export the functions to be used in routes
module.exports = {
  register,
  login,
  getUserById,
  getAllUsers,
  updateUser
};