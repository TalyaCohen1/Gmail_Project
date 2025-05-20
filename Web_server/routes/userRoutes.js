const express = require('express');
const router = express.Router();
const usersController = require('../controllers/usersController');

router.post('/', usersController.register);

router.post('/login', usersController.login);

router.get('/:id', usersController.getUserById);

module.exports = router;
