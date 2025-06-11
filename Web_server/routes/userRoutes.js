const express = require('express');
const router = express.Router();
const usersController = require('../controllers/userController');
const upload = require('../middlewares/upload');

//create a new user
//POST /api/users
router.post('/', upload.single('profileImage') ,usersController.register);

//login a user
//POST /api/users/login
router.post('/login', usersController.login);
//get all users (for checking)
//GET /api/users
router.get('/', usersController.getAllUsers);
//get a user by id
//GET /api/users/:id
router.get('/:id', usersController.getUserById);

module.exports = router;
