// src/models/mailModel.js

let inbox = [];
let nextId = 1;
let sent = [];

/**
 * Return up to 50 most recent mails (inbox + sent), sorted by timestamp descending.
 */
function getAll() {
    const all = inbox.concat(sent);
    return all.sort((a, b) => b.timestamp - a.timestamp).slice(0, 50);
}

/**
 * Find a mail by its ID in either inbox or sent.
 * @returns the mail object or null if not found.
 */
function getById(id) {
    const found =
        inbox.find(m => m.id === id) ||
        sent.find(m  => m.id === id);
    return found || null;
}

/**
 * Search all mails (inbox + sent) for those whose
 * subject or body contains the given query (case‐insensitive).
 * @returns an array (possibly empty) of matching mails.
 */
function search(query) {
    const ql = query.toLowerCase();
    return inbox.concat(sent).filter(m =>
        m.subject.toLowerCase().includes(ql) ||
        m.body.toLowerCase().includes(ql)
        );
}

// does this work?
/**
 * Create a new mail entry:
 * - Assign a unique ID
 * - Add a timestamp
 * - Push a copy into inbox (as a received mail) and into sent (as a sent mail)
 * @returns the newly created mail object.
 */
function createMail({ from, to, subject, body }) {
    const timestamp = Date.now();
    const mail = { id: nextId++, from, to, subject, body, timestamp };

    inbox.push({ ...mail, mailbox: 'inbox' });
    sent .push({ ...mail, mailbox: 'sent' });

    return mail;
}

/**
 * Update subject and/or body of an existing mail.
 * Returns the updated mail, or null if no mail with that ID exists.
 */
function updateMail(id, fields) {
    let updated = null;

    [inbox, sent].forEach(arr => {
        arr.forEach(m => {
        if (m.id === id) {
            if (fields.subject !== undefined) m.subject = fields.subject;
            if (fields.body    !== undefined) m.body    = fields.body;
            updated = m;
        }
        });
    });

    return updated;
}

/**
 * Delete a mail by ID from both inbox and sent.
 * @returns true if at least one mail was deleted, false otherwise.
 */
function deleteMail(id) {
    let removed = false;

    [inbox, sent].forEach(arr => {
        const idx = arr.findIndex(m => m.id === id);
        if (idx !== -1) {
        arr.splice(idx, 1);
        removed = true;
        }
    });

    return removed;
}

module.exports = {
    getAll,
    getById,
    createMail,
    search,
    updateMail,
    deleteMail
};
