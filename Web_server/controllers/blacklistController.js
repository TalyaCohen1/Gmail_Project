const service = require('../services/blacklistService');

exports.addToBlacklist = (req, res) => {
    service.addUrl(req, res);
};

exports.deleteFromBlacklist = (req, res) => {
    service.removeUrl(req, res);
};

exports.checkBlacklist = (req, res) => {
    service.checkUrl(req, res);
};
