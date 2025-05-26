const Link = require('../models/blacklistModel')
const service = require('../services/blacklistService')

exports.addToBlacklist = (req, res) => {
    const { url } = req.body;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }
    const newLink = Link.addToBlacklist(url);
    res.status(201).location(`/api/blacklist/${newLink.url}`).end();
}

exports.deleteFromBlacklist = (req, res) => {
    const link = Link.getLink(req.params.url);
    if (!link) {
        return res.status(404).json({ error: 'Link not found' });
    }
    Link.deleteFromBlacklist(link.url);
    res.status(204).json();
}