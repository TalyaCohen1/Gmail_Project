// blacklistRoutes.js
const express = require('express');
const router = express.Router();
const controller = require('../controllers/blacklistController');

// POST /api/blacklist - Add URL to blacklist
//router.post('/', blacklistController.addToBlacklist);

// DELETE /api/blacklist/:id - Remove URL from blacklist
//router.delete('/:id', blacklistController.deleteFromBlacklist);

router.route('/')
        .post(controller.addToBlacklist);

router.route('/:id')
        .delete(controller.deleteFromBlacklist);


module.exports = router;