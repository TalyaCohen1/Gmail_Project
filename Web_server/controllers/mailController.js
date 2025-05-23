// src/controllers/mailController.js

const net = require('net');               // Node.js core TCP module
const mailModel = require('../models/mailModel');

const URL_REGEX = /https?:\/\/[^\s]+/g;

/**
 * Check a single URL against the C++ Blacklist server.
 * Sends "GET <url>\n" over TCP, collects the response,
 * and resolves to true if the URL is blacklisted.
 *
 * Expected response format:
 *   200 Ok\n
 *   \n
 *    true true\n
 * or 200 Ok\n\n true false\n, etc.
 */
function checkBlacklist(url, host = '127.0.0.1', port = 4000) {
    return new Promise((resolve, reject) => {
        let buffer = '';
        const client = net.createConnection({ host, port }, () => {
            client.write(`GET ${url}\n`);
        });

        client.on('data', chunk => {
            buffer += chunk.toString();
        });

        client.on('end', () => {
            const parts = buffer.split('\n');
            // parts[2] should look like " true true" or " true false"
            const flags = (parts[2] || '').trim().split(/\s+/);
            // if any flag === 'true', consider it blacklisted
            const isBlacklisted = flags.some(f => f === 'true');
            resolve(isBlacklisted);
        });

        client.on('error', err => reject(err));
    });
}

/**
 * GET /api/mails
 * Return JSON list of up to 50 most recent mails.
 */
exports.listMails = (req, res) => {
    const mails = mailModel.getAll();
    res.status(200).json(mails);
};

/**
 * GET /api/mails/:id
 * Return a single mail by ID, or 404 if not found.
 */
exports.getMail = (req, res) => {
    const id = Number(req.params.id);
    const mail = mailModel.getById(id);
    if (!mail) return res.status(404).json({ error: 'Mail not found' });
    res.status(200).json(mail);
};

/**
 * GET /api/mails/search/:query
 * Return all mails whose subject or body contains the query string.
 */
exports.searchMails = (req, res) => {
    const q = req.params.query;
    const results = mailModel.search(q);
    res.status(200).json(results);
};

/**
 * POST /api/mails
 * Create a new mail after verifying no blacklisted URLs.
 * Returns 201 with Location header, or 400 on bad request,
 * or 500 if the blacklist check fails.
 */
exports.sendMail = async (req, res) => {
    const { from, to, subject, body } = req.body;

    // Validate required fields
    if (!from || !to || !subject || !body) {
        return res.status(400).json({ error: 'Missing fields' });
    }

    // Extract URLs from the body
    const urls = body.match(URL_REGEX) || [];

    // Check each URL against the blacklist server
    for (const url of urls) {
        let bad;
        try {
            bad = await checkBlacklist(url);
        } catch (e) {
            return res.status(500).json({ error: 'Blacklist service error' });
        }
        if (bad) {
            return res.status(400).json({ error: 'Contains blacklisted link' });
        }
    }

    // All URLs are clean → create the mail
    const mail = mailModel.createMail({ from, to, subject, body });

    res.status(201).location(`/api/mails/${mail.id}`).json(mail);
};

/**
 * PATCH /api/mails/:id
 * – Updates subject and/or body
 * – Rejects with 400 if no fields, 404 if mail not found
 * – Also rejects with 400 if any new URL is blacklisted
 */
exports.updateMail = async (req, res) => {
    const id = Number(req.params.id);
    const { subject, body } = req.body;

    // 1) Must supply at least one of subject/body
    if (subject === undefined && body === undefined) {
        return res.status(400).json({ error: 'No fields to update' });
    }

    // 2) Collect any URLs in the new subject and/or body
    const subjectUrls = subject ? subject.match(URL_REGEX) || [] : [];
    const bodyUrls    = body    ? body.match(URL_REGEX)    || [] : [];
    const allUrls     = [...subjectUrls, ...bodyUrls];

    // 3) Check each URL against the blacklist
    for (const url of allUrls) {
        let isBlacklisted;
        try {
            isBlacklisted = await checkBlacklist(url);
        } catch (e) {
            return res.status(500).json({ error: 'Blacklist service error' });
        }
        if (isBlacklisted) {
            return res.status(400).json({ error: 'Contains blacklisted link' });
        }
    }

    // 4) Perform the update in the model
    const updated = mailModel.updateMail(id, { subject, body });
    if (!updated) {
        return res.status(404).json({ error: 'Mail not found' });
    }

    // 5) Return the updated mail
    res.status(200).json(updated);
};

/**
 * DELETE /api/mails/:id
 * Remove a mail by ID from both inbox and sent.
 * Returns 204 on success or 404 if not found.
 */
exports.deleteMail = (req, res) => {
    const id = Number(req.params.id);
    const removed = mailModel.deleteMail(id);
    if (!removed) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.sendStatus(204);
};
