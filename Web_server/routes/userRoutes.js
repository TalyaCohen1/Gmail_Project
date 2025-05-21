const express = require('express');
const router = express.Router();
const usersController = require('../controllers/usersController');

//create a new user
router.post('/', usersController.register);

//login a user
router.post('/login', usersController.login);

//get a user by id
router.get('/:id', usersController.getUserById);

module.exports = router;
