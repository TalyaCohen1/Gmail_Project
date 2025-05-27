const express = require('express');
const router = express.Router();
const controller = require('../controllers/labelController');
const { authenticateUser } = require('../middlewares/authMiddleware');

router.route('/')
        .get(authenticateUser, controller.getAllLabels)
        .post(authenticateUser, controller.createLabel);

router.route('/:id')
        .get(authenticateUser, controller.getLabelById)
        .patch(authenticateUser, controller.updateLabel)
        .delete(authenticateUser, controller.deleteLabel);

module.exports = router;