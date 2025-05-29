const express = require('express');
const router = express.Router();
const usersController = require('../controllers/userController');

//create a new user
router.post('/', usersController.register);

//login a user
router.post('/login', usersController.login);
//get all users (for checking)
router.get('/', usersController.getAllUsers);
//get a user by id
router.get('/:id', usersController.getUserById);

router.get('/me', usersController.getUser);

module.exports = router;
