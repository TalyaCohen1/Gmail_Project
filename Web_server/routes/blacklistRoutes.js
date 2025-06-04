const express = require('express');
const router = express.Router();
const controller = require('../controllers/blacklistController');
const { authenticateUser } = require('../middlewares/authMiddleware');

/**
 * Route to add a URL to the blacklist.
 * POST /api/blacklist
 */
router.route('/')
    .post(authenticateUser,controller.addToBlacklist);

/**
 * Route to remove a URL from the blacklist by ID.
 * DELETE /api/blacklist/:id
 */
router.route('/:id')
    .delete(authenticateUser,controller.deleteFromBlacklist);

module.exports = router;
