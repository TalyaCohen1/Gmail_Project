let mails = [];
let nextId = 1;

/**
 * Return up to 50 most recent mails for this user (sent or received).
 * @param {string} userId
 */
function getAll(userId) {
    return mails
        .filter(m => m.to === userId || m.from === userId)
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 50);
}

/**
 * Find one mail by ID for this user; null if not found or not theirs.
 * @param {string} userId
 * @param {number} id
 */
function getById(userId, id) {
    return (
        mails.find(
            m => m.id === id && (m.from === userId || m.to === userId)
        ) || null
    );
}

/**
 * Search this user’s mails for query in subject or body.
 * @param {string} userId
 * @param {string} query
 */
function search(userId, query) {
    const ql = query.toLowerCase();
    return mails.filter(m =>
        (m.from === userId || m.to === userId) &&
        (m.subject.toLowerCase().includes(ql) ||
        m.body.toLowerCase().includes(ql))
    );
}

/**
 * Create a new mail record.
 * @param {string} from    the sender’s userId
 * @param {string} to      the recipient’s userId
 * @param {string} subject
 * @param {string} body
 * @returns the newly created mail object
 */
function createMail({ from, to, subject, body }) {
    from = String(from);
    to   = String(to);
    const timestamp = Date.now();
    const mail = { id: nextId++, from, to, subject, body, timestamp };
    mails.push(mail);
    return mail;
}

/**
 * Update an existing mail’s subject/body,
 * only if userId is the sender and there's no blacklisted URLs.
 * Returns updated mail or null.
 * @param {string} userId
 * @param {number} id
 * @param {{subject?:string,body?:string}} fields
 */
function updateMail(userId, id, fields) {
    const m = mails.find(m => m.id === id && m.from === userId);
    if (!m) return null;
    if (fields.subject !== undefined) m.subject = fields.subject;
    if (fields.body !== undefined) m.body = fields.body;
    return m;
}

/**
 * Delete a mail, only if userId is sender or recipient.
 * @param {string} userId
 * @param {number} id
 * @returns true if deleted, false otherwise
 */
function deleteMail(userId, id) {
    const idx = mails.findIndex(
        m => m.id === id && (m.from === userId || m.to === userId)
    );
    if (idx === -1) return false;
    mails.splice(idx, 1);
    return true;
}

module.exports = {
    getAll,
    getById,
    createMail,
    search,
    updateMail,
    deleteMail
};
