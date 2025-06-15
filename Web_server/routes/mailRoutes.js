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

// GET all drafts
router.get('/drafts', authenticateToken, controller.getDrafts);

// GET inbox mails
router.get('/inbox', authenticateToken, controller.getInbox);

// GET sent mails
router.get('/sent', authenticateToken, controller.getSent);

// Retrieve a single mail by ID
router.get('/:id', authenticateToken, controller.getMail);

// Update an existing mail
router.patch('/:id', authenticateToken, controller.updateDraft);

// Delete a mail
router.delete('/:id', authenticateToken, controller.deleteMail);

// POST DELETE and GET to labels
router.post('/:id/labels', authenticateUser, controller.addLabel);
router.delete('/:id/labels/:labelId', authenticateUser, controller.removeLabel);
router.get('/:id/labels', authenticateUser, controller.getLabels);


module.exports = router;
