const express = require('express');
const router = express.Router();
const tokensController = require('../controllers/tokenController');

//ger a token by usernuame+ password
router.post('/', tokensController.createToken);

module.exports = router;
