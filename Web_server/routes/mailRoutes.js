// src/routes/mailsRoutes.js

const express = require('express');
const router = express.Router();
const ctl = require('../controllers/mailController');

// List up to 50 most recent mails
router.get('/', ctl.listMails);

// Create a new mail (with blacklist check)
router.post('/', ctl.sendMail);

// Search mails by query string in subject or body must come before '/:id' to avoid routing conflicts
router.get('/search/:query', ctl.searchMails);

// Retrieve a single mail by ID
router.get('/:id', ctl.getMail);

// Update an existing mail
router.patch('/:id', ctl.updateMail);

// Delete a mail
router.delete('/:id', ctl.deleteMail);

module.exports = router;
