const express = require('express');
const router = express.Router();
const controller = require('../controllers/labelController');
const { authenticateUser } = require('../middlewares/authMiddleware');

/**
 * Route to get all labels for authenticated user or create a new label.
 * GET /api/labels
 * POST /api/labels
 */
router.route('/')
    .get(authenticateUser, controller.getAllLabels)
    .post(authenticateUser, controller.createLabel);

/**
 * Route to get, update, or delete a label by ID for authenticated user.
 * GET /api/labels/:id
 * PATCH /api/labels/:id
 * DELETE /api/labels/:id
 */
router.route('/:id')
    .get(authenticateUser, controller.getLabelById)
    .patch(authenticateUser, controller.updateLabel)
    .delete(authenticateUser, controller.deleteLabel);

module.exports = router;
