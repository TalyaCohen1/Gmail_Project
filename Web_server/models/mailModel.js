let mails = [];
let nextId = 1;
let draftMails = [];

/**
 * Return up to 50 most recent mails for this user (sent or received).
 * @param {string} email
 */
function getAll(email) {
    return mails
        .filter(m => 
            (m.to === email && !m.deletedForReceiver) || 
            (m.from === email && !m.deletedForSender)
        )
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 50);
}

/**
 * Find one mail by ID for this user; null if not found or not theirs.
 * @param {string} email
 * @param {number} id
 */
function getById(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail || (mail.from === email && mail.deletedForSender) || (mail.to === email && mail.deletedForReceiver)) {
        return null;
    }
    return mail;
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

function createDraft({ from, to, subject, body}) {
    const timestamp = Date.now();
    const draft = { id: nextId++, from, to, subject, body, timestamp };
    draftMails.push(draft);
    return draft;
}

/**
 * Create a new mail record.
 * @param {string} from    the sender’s email
 * @param {string} to      the recipient’s email
 * @param {string} subject
 * @param {string} body
 * 
 * @returns the newly created mail object
 */
function createMail({ from, to, subject, body, id = nextId++ }) {
    const timestamp = Date.now();
    const mail = { id, from, to, subject, body, timestamp , deletedForSender: false, deletedForReceiver: false };
    mails.push(mail);
    return mail;
}

/**
 * Update an existing mail’s subject/body,
 * only if email is the sender's email and there's no blacklisted URLs.
 * Returns updated mail or null.
 * @param {string} email
 * @param {number} id
 * @param {{subject?:string,body?:string,to?:string, send?: boolean}} fields
 */
function updateDraft(email, id, fields) {
    const d = draftMails.find(d => String(d.id) === String(id) && d.from === email);
    if (!d) return null;

    if (fields.subject !== undefined) d.subject = fields.subject;
    if (fields.body !== undefined) d.body = fields.body;
    if (fields.to !== undefined) d.to = fields.to;

    if(fields.send === false){
        return d;
    } else {
        const mail = createMail({ from: d.from, to: d.to, subject: d.subject, body: d.body, id });
        // Remove the draft after sending
        const idx = deleteFromDrafts(id);
        if (!idx) return null;
        return mail;
    }
}

function deleteFromDrafts(id) {
    const idx = draftMails.findIndex(d => d.id === id);
    if (idx === -1) return false;
    draftMails.splice(idx, 1);
    return true;
}

/**
 * Delete a mail, only if email is the email of the sender or recipient.
 * @param {string} email
 * @param {number} id
 * @returns true if deleted, false otherwise
 */
function deleteMail(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return false;
    if (mail.from === email) {
        mail.deletedForSender = true;
    } else {
        mail.deletedForReceiver = true;
    }
    return true;
}

module.exports = {
    getAll,
    getById,
    createMail,
    search,
    createDraft,
    updateDraft,
    deleteMail
};
