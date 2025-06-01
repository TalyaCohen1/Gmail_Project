const express = require('express');
const router = express.Router();
const controller = require('../controllers/blacklistController');

/**
 * Route to add a URL to the blacklist.
 * POST /api/blacklist
 */
router.route('/')
    .post(controller.addToBlacklist);

/**
 * Route to remove a URL from the blacklist by ID.
 * DELETE /api/blacklist/:id
 */
router.route('/:id')
    .delete(controller.deleteFromBlacklist);

module.exports = router;
