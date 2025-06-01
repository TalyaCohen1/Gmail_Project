let mails = [];
let nextId = 1;

/**
 * Return up to 50 most recent mails for this user (sent or received).
 * @param {string} email
 */
function getAll(email) {
    return mails
        .filter(m => m.to === email || m.from === email)
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 50);
}

/**
 * Find one mail by ID for this user; null if not found or not theirs.
 * @param {string} email
 * @param {number} id
 */
function getById(email, id) {
    return (
        mails.find(
            m => m.id === id && (m.from === email || m.to === email)
        ) || null
    );
}

/**
 * Search this user’s mails for query in subject or body.
 * @param {string} email
 * @param {string} query
 */
function search(email, query) {
    const ql = query.toLowerCase();
    return mails.filter(m =>
        (m.from === email || m.to === email) &&
        (m.subject.toLowerCase().includes(ql) ||
        m.body.toLowerCase().includes(ql))
    );
}

/**
 * Create a new mail record.
 * @param {string} from    the sender’s email
 * @param {string} to      the recipient’s email
 * @param {string} subject
 * @param {string} body
 * @returns the newly created mail object
 */
function createMail({ from, to, subject, body }) {
    const timestamp = Date.now();
    const mail = { id: nextId++, from, to, subject, body, timestamp };
    mails.push(mail);
    return mail;
}

/**
 * Update an existing mail’s subject/body,
 * only if email is the sender's email and there's no blacklisted URLs.
 * Returns updated mail or null.
 * @param {string} email
 * @param {number} id
 * @param {{subject?:string,body?:string}} fields
 */
function updateMail(email, id, fields) {
    const m = mails.find(m => String(m.id) === String(id) && m.from === email);
    if (!m) return null;
    if (fields.subject !== undefined) m.subject = fields.subject;
    if (fields.body !== undefined) m.body = fields.body;
    return m;
}

/**
 * Delete a mail, only if email is the email of the sender or recipient.
 * @param {string} email
 * @param {number} id
 * @returns true if deleted, false otherwise
 */
function deleteMail(email, id) {
    const idx = mails.findIndex(
        m => m.id === id && (m.from === email || m.to === email)
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
