const express = require('express');
const router = express.Router();
const controller = require('../controllers/mailController');
const { authenticateUser } = require('../middlewares/authMiddleware');

// List up to 50 most recent mails
router.get('/', authenticateUser, controller.listMails);

// Create a new mail (with blacklist check)
router.post('/', authenticateUser, controller.sendMail);

// Search mails by query string in subject or body must come before '/:id' to avoid routing conflicts
router.get('/search/:query', authenticateUser, controller.searchMails);

// Retrieve a single mail by ID
router.get('/:id', authenticateUser, controller.getMail);

// Update an existing mail
router.patch('/:id', authenticateUser, controller.updateDraft);

// Delete a mail
router.delete('/:id', authenticateUser, controller.deleteMail);

// POST DELETE and GET to labels
router.post('/:id/labels', authenticateUser, controller.addLabel);
router.delete('/:id/labels/:labelId', authenticateUser, controller.removeLabel);
router.get('/:id/labels', authenticateUser, controller.getLabels);

module.exports = router;
