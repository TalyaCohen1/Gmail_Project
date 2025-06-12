const express = require('express');
const router = express.Router();
const controller = require('../controllers/mailController');
const { authenticateUser , authenticateToken} = require('../middlewares/authMiddleware');

// List up to 50 most recent mails
router.get('/', authenticateToken, controller.listMails);

// Create a new mail (with blacklist check)
router.post('/', authenticateToken, controller.sendMail);

// Search mails by query string in subject or body must come before '/:id' to avoid routing conflicts
router.get('/search/:query', authenticateToken, controller.searchMails);

// Retrieve a single mail by ID
router.get('/:id', authenticateToken, controller.getMail);

// Update an existing mail
router.patch('/:id', authenticateToken, controller.updateDraft);

// Delete a mail
router.delete('/:id', authenticateToken, controller.deleteMail);

module.exports = router;
