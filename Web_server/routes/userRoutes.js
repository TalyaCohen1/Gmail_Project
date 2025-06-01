const express = require('express');
const router = express.Router();
const usersController = require('../controllers/userController');

//create a new user
//POST /api/users
router.post('/', usersController.register);

//login a user
//POST /api/users/login
router.post('/login', usersController.login);
//get all users (for checking)
//GET /api/users
router.get('/', usersController.getAllUsers);
//get a user by id
//GET /api/users/:id
router.get('/:id', usersController.getUserById);
//get user info using token from header
//GET /api/users/me
router.get('/me', usersController.getUser);

module.exports = router;
