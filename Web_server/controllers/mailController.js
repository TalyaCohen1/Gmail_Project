const mailModel = require('../models/mailModel');
const userModel = require('../models/userModel');
const { checkUrl } = require('../services/blacklistService');
//const URL_REGEX = /https?:\/\/[^\s]+/g;
const URL_REGEX = /\b(?:https?:\/\/)?(?:www\.)?[^\s]+\.[^\s]+\b/g;

/**
 * GET /api/mails
 */
exports.listMails = (req, res) => {
    const mails = mailModel.getAll(req.userId);
    res.status(200).json(mails);
};

/**
 * GET /api/mails/:id
 */
exports.getMail = (req, res) => {
    const id = Number(req.params.id);
    const mail = mailModel.getById(req.userId, id);
    if (!mail) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.status(200).json(mail);
};

/**
 * GET /api/mails/search/:query
 */
exports.searchMails = (req, res) => {
    const query = req.params.query;
    const results = mailModel.search(req.userId, query);
    res.status(200).json(results);
};

/**
 * POST /api/mails
 */
exports.sendMail = async (req, res) => {
    try {
        const from = req.userId;
        const { to, subject, body } = req.body;

        if (!to || !subject || !body) {
            return res.status(400).json({ error: 'Missing fields' });
        }

        const toUser = await userModel.findById(to);
        if (!toUser) {
            return res.status(400).json({ error: 'Recipient does not exist' });
        }

        const urls = body.match(URL_REGEX) || [];
        const urlsFromSubject = subject.match(URL_REGEX) || [];
        
        for (const url of urls) {
            //const normalized = url.replace(/^https?:\/\//, '');
            const blacklisted = await checkUrl(url);
            if (blacklisted) {
                return res.status(400).json({ error: `Contains blacklisted link: ${url}` });
            }
        }
        for (const url of urlsFromSubject) {
            //const normalized = url.replace(/^https?:\/\//, '');
            const blacklisted = await checkUrl(url);
            if (blacklisted) {
                return res.status(400).json({ error: `Contains blacklisted link: ${url}` });
            }
        }
        const mail = await mailModel.createMail({ from, to, subject, body });

        res.status(201)
            .location(`/api/mails/${mail.id}`)
            .json(mail);

    } catch (error) {
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * PATCH /api/mails/:id
 */
exports.updateMail = async (req, res) => {
    const id = Number(req.params.id);
    const { subject, body } = req.body;

    if (subject === undefined && body === undefined) {
        return res.status(400).json({ error: 'No fields to update' });
    }

    const subjectUrls = subject ? subject.match(URL_REGEX) || [] : [];
    const bodyUrls = body ? body.match(URL_REGEX) || [] : [];
    const allUrls = [...subjectUrls, ...bodyUrls];

    for (const url of allUrls) {
        try {
            //const blacklisted = await isBlacklisted(url);
            //const normalized = url.replace(/^https?:\/\//, '');
            const blacklisted = await checkUrl(url);

            if (blacklisted) {
                return res.status(400).json({ error: `Contains blacklisted link: ${url}` });
            }
        } catch (e) {
            return res.status(500).json({ error: 'Blacklist service error' });
        }
    }

    const updated = mailModel.updateMail(req.userId, id, { subject, body });

    if (!updated) {
        return res.status(404).json({ error: 'Mail not found' });
    }

    res.status(200).json(updated);
};

/**
 * DELETE /api/mails/:id
 */
exports.deleteMail = (req, res) => {
    const id = Number(req.params.id);
    const removed = mailModel.deleteMail(req.userId, id);
    if (!removed) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.sendStatus(204);
};
