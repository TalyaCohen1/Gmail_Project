const { get } = require("../routes/mailRoutes");

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
        .slice(0, 25);
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
    const date = new Date().toISOString()
    const draft = { id: nextId++, from, to, subject, body, date, timestamp };
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
function createMail({ from, to, subject, body, id }) {
    if (id === undefined || id === null) {
        id = nextId++;
    }
    const timestamp = Date.now();
    const date = new Date().toISOString()
    const mail = { id, from, to, subject, body, date, timestamp , deletedForSender: false, deletedForReceiver: false, labelsForSender: [],
        labelsForReceiver: [] };
    
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

function getDrafts(email) {
    return draftMails.filter(d => d.from === email);
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

/**
 * Add a label to a mail.
 * @param {string} email
 * @param {number} id
 * @param {string} labelId
 * @returns true if added, false otherwise
 */
function addLabel(email, id, labelId) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return false;
    if (mail.from === email) {
        if (!mail.labelsForSender.includes(labelId)) {
            mail.labelsForSender.push(labelId);
        }
    } else if (mail.to === email) {
        if (!mail.labelsForReceiver.includes(labelId)) {
            mail.labelsForReceiver.push(labelId);
        }
        return true;
    }
    return false;
}


function removeLabel(email, id, labelId) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return false;
    if (mail.from === email) {
        mail.labelsForSender = mail.labelsForSender.filter(l => l !== labelId);
    } else if (mail.to === email) {
        mail.labelsForReceiver = mail.labelsForReceiver.filter(l => l !== labelId);
    } else {
        return false; // Not the owner of the mail
    }
    mail.labelsForSender = mail.labelsForSender.filter(l => l !== labelId);
    mail.labelsForReceiver = mail.labelsForReceiver.filter(l => l !== labelId);
    return true;
}

function getLabels(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return null;
    if (mail.from === email) {
        return mail.labelsForSender;
    }
    if (mail.to === email) {
        return mail.labelsForReceiver;
    }
}

function getInbox(email) {
  return mails
    .filter(m => m.to === email && !m.deletedForReceiver)
    .sort((a,b) => b.timestamp - a.timestamp)
    .slice(0,25);
}

function getSent(email) {
  return mails
    .filter(m => m.from === email && !m.deletedForSender)
    .sort((a,b) => b.timestamp - a.timestamp)
    .slice(0,25);
}

module.exports = {
    getAll,
    getById,
    createMail,
    search,
    createDraft,
    updateDraft,
    getDrafts,
    deleteMail,
    addLabel,
    removeLabel,
    getLabels,
    getInbox,
    getSent
};
