const userModel = require('../models/userModel');
const { isValidGmail, isValidDateFormat, isPastDate, isAgeOver13, isValidGender, isValidPassword } = require('../models/validator');
const labelModel = require('../models/labelModel');

/**
 * register a new user
 * POST /api/users
 * @param {*} req - The request object containing user data
 * @param {*} res  - The response object to send back the result
 */
const register = async (req, res) => {
  const { fullName, emailAddress, birthDate, gender, password } = req.body;
  // profileImage is set by multer in req.file, or defaulted here.
  // Mongoose schema default will also handle this if profileImage is not explicitly set in createUser.
  const profileImage = req.file
    ? `/uploads/${req.file.filename}`
    : '/uploads/default-profile.png'; // This path is correct for DB storage and return

  if (!fullName || !password || !emailAddress || !birthDate || !gender) {
    return res.status(400).json({ error: "Missing required fields" });
  }
  if (!isValidGmail(emailAddress)) {
    return res.status(400).json({ error: "Email must be a valid @gmail.com address" });
  }

  if (!isValidDateFormat(birthDate)) {
    return res.status(400).json({ error: "Birth date must be in the formatYYYY-MM-DD" });
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
    return res.status(400).json({ error: "Gender must be either 'male' or 'female'" });
  }

  try {
    const existingUser = await userModel.findByEmail(emailAddress);
    if (existingUser) {
      return res.status(400).json({ error: 'This email address already exists' });
    }

    const newUser = await userModel.createUser(fullName, emailAddress, birthDate, gender, password, profileImage);

    await labelModel.createDefaultLabels(newUser._id);

    res.status(201)
      .location(`/api/users/${newUser._id}`)
      .json({
        _id: newUser._id,
        id: newUser._id,
        fullName: newUser.fullName,
        profileImage: newUser.profileImage || '/uploads/default-profile.png' // Ensure fallback here too
      });
  } catch (error) {
    console.error("Error during user registration:", error);
    res.status(500).json({ error: "Internal server error during registration" });
  }
};

/**
 * Login a user
 * Post /api/users/login
 * @param {*} req - The request object containing email and password
 * @param {*} res - The response object to send back the result
 */
const login = async (req, res) => {
  const { emailAddress, password } = req.body;

  try {
    const user = await userModel.findByEmail(emailAddress);

    if (!user || user.password !== password) {
      return res.status(400).json({ error: 'Wrong email or password!' });
    }

    res.status(200).json({
      token: user._id,
      _id: user._id,
      id: user._id,
      fullName: user.fullName,
      emailAddress: user.emailAddress,
      profileImage: user.profileImage || '/uploads/default-profile.png' // IMPORTANT: Ensure fallback here
    });
  } catch (error) {
    console.error("Error during user login:", error);
    res.status(500).json({ error: "Internal server error during login" });
  }
};

/**
 * Get user by ID from URL
 * GET /api/users/:id
 * @param {*} req - request object
 * @param {*} res - response object
 */
const getUserById = async (req, res) => {
  try {
    const user = await userModel.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.status(200).json({
      _id: user._id,
      id: user._id,
      fullName: user.fullName,
      emailAddress: user.emailAddress,
      birthDate: user.birthDate,
      gender: user.gender,
      profileImage: user.profileImage || '/uploads/default-profile.png' // IMPORTANT: Ensure fallback here
    });
  } catch (error) {
    console.error("Error getting user by ID:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

/**
 * Get all users (for admin/testing)
 * GET /api/users
 * @param {*} req - request object
 * @param {*} res - response object
 */
const getAllUsers = async (req, res) => {
  try {
    const users = await userModel.getAllUsers();
    const formattedUsers = users.map(user => ({
      _id: user._id,
      id: user._id,
      fullName: user.fullName,
      emailAddress: user.emailAddress,
      profileImage: user.profileImage || '/uploads/default-profile.png' // IMPORTANT: Ensure fallback here
    }));
    res.status(200).json(formattedUsers);
  } catch (error) {
    console.error("Error getting all users:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

/**
 * Update user information
 * PATCH /api/users/:id
 * @param {*} req - request object
 * @param {*} res - response object
 */
const updateUser = async (req, res) => {
  const userId = req.params.id;
  const { fullName } = req.body;
  let updates = {};

  if (fullName) {
    updates.fullName = fullName;
  }
  if (req.file) {
    updates.profileImage = `/uploads/${req.file.filename}`;
  } else if ('profileImage' in req.body && req.body.profileImage === null) {
      // Allow explicit setting to null/empty string to trigger default fallback
      updates.profileImage = null; // Or an empty string
  }


  if (Object.keys(updates).length === 0) {
    return res.status(400).json({ error: 'No valid fields provided for update.' });
  }

  try {
    const updatedUser = await userModel.updateUser(userId, updates);
    if (!updatedUser) {
      return res.status(404).json({ error: 'User not found or failed to update' });
    }

    return res.json({
      message: 'User updated successfully',
      _id: updatedUser._id,
      id: updatedUser._id,
      fullName: updatedUser.fullName,
      emailAddress: updatedUser.emailAddress,
      profileImage: updatedUser.profileImage || '/uploads/default-profile.png', // IMPORTANT: Ensure fallback here
    });
  } catch (error) {
    console.error("Error updating user:", error);
    res.status(500).json({ error: "Internal server error during update" });
  }
};

module.exports = {
  register,
  login,
  getUserById,
  getAllUsers,
  updateUser
};
