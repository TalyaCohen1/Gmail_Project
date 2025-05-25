const express = require('express');
const router = express.Router();
const controller = require('../controllers/labelController');

router.route('/')
        .get(controller.getAllLabels)
        .post(controller.createLabel);

router.route('/:id')
        .get(controller.getLabelById)
        .patch(controller.updateLabel)
        .delete(controller.deleteLabel);

module.exports = router;