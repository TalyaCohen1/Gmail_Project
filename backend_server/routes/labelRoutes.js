const express = require('express');
const router = express.Router();
const controller = require('../controllers/labelController');
const authenticateToken = require('../middlewares/authMiddleware');

/**
 * Route to get all labels for authenticated user or create a new label.
 * GET /api/labels
 * POST /api/labels
 */
router.route('/')
    .get(authenticateToken, controller.getAllLabels)
    .post(authenticateToken, controller.createLabel);

/**
 * Route to get, update, or delete a label by ID for authenticated user.
 * GET /api/labels/:id
 * PATCH /api/labels/:id
 * DELETE /api/labels/:id
 */
router.route('/:id')
    .get(authenticateToken, controller.getLabelById)
    .patch(authenticateToken, controller.updateLabel)
    .delete(authenticateToken, controller.deleteLabel);

router.route('/:id/mails')
    .post(authenticateToken, controller.addMailToLabel)
    .delete(authenticateToken, controller.removeMailFromLabel)
    .get(authenticateToken, controller.getMailsByLabel);

module.exports = router;
