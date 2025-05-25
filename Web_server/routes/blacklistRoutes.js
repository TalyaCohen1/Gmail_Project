const express = require('express');
const router = express.Router();

// מסלול בדיקה בסיסי
router.get('/', (req, res) => {
  res.send('Blacklist route is active');
});

module.exports = router;
