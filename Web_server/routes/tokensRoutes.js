const express = require('express');
const router = express.Router();
const tokensController = require('../controllers/tokensController');

router.post('/', tokensController.generateToken);

module.exports = router;
