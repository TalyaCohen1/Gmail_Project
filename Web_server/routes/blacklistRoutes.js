const express = require('express');
const router = express.Router();
const controller = require('../controllers/blacklistController');
const { authenticateUser, authenticateToken } = require('../middlewares/authMiddleware');

/**
 * Route to add a URL to the blacklist.
 * POST /api/blacklist
 */
router.route('/')
    .post(authenticateToken,controller.addToBlacklist);

/**
 * Route to remove a URL from the blacklist by ID.
 * DELETE /api/blacklist/:id
 */
router.route('/:id')
    .delete(authenticateToken,controller.deleteFromBlacklist);

module.exports = router;