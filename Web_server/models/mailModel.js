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
            ((m.to === email && !m.deletedForReceiver) ||
            (m.from === email && !m.deletedForSender)) && !m.isSpam
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

function getDraftById(email, id) {
    const draft = draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (!draft) {
        return null;
    }
    return draft;
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
        m.body.toLowerCase().includes(ql)) && !m.isSpam
    );
}

function createDraft({ from, to, subject, body, isImportant = false, isStarred = false, isSpam = false, labels = []}) {
    const timestamp = Date.now();
    const date = new Date().toISOString()
    const draft = { id: nextId++, from, to, subject, body, date, timestamp, send: false, isImportant, isStarred, isSpam, labels, deleted: false }; // Added send: false
    draftMails.push(draft);
    return draft;
}

/**
 * Create a new mail record.
 * @param {string} from    the sender’s email
 * @param {string} to      the recipient’s email
 * @param {string} subject
 * @param {string} body
 * @param {boolean} isSpam
 *
 * @returns the newly created mail object
 */
function createMail({ from, to, subject, body, id, isSpam = false, isImportant = false, isStarred = false, labels = [] }) {
    if (id === undefined || id === null) {
        id = nextId++;
    }
    console.log(`mailMOdel ${isSpam}`);
    const timestamp = Date.now();
    const date = new Date().toISOString()
    const mail = { id, from, to, subject, body, date, timestamp , deletedForSender: false, deletedForReceiver: false, labelsForSender: labels,
        labelsForReceiver: [], isSpam, isRead: false, isImportant, isStarred, send: true }; // Added send: true

    mails.push(mail);
    return mail;
}


/**
 * Update an existing mail’s subject/body,
 * only if email is the sender's email and there's no blacklisted URLs.
 * Returns updated mail or null.
 * @param {string} email
 * @param {number} id
 * @param {{subject?:string,body?:string,to?:string, send?: boolean, isImportant?: boolean, isStarred?: boolean, labels?: string[]}} fields
 */
function updateDraft(email, id, fields) {
    const d = draftMails.find(d => String(d.id) === String(id) && d.from === email);
    if (!d || d.deleted) return null;

    if (fields.subject !== undefined) d.subject = fields.subject;
    if (fields.body !== undefined) d.body = fields.body;
    if (fields.to !== undefined) d.to = fields.to;

    if(fields.send === false){
        return d;
    } else {
        const mail = createMail({ from: d.from, to: d.to, subject: d.subject, body: d.body, id, isSpam: d.isSpam, isImportant: d.isImportant, isStarred: d.isStarred, labels: d.labels });
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
    return draftMails.filter(d => d.from === email && d.send === false && !d.deleted); // Filter by send: false
}


/**
 * Delete a mail, only if email is the email of the sender or recipient.
 * If the mail is spam and being deleted, its associated URLs are removed from the blacklist.
 * @param {string} email
 * @param {number} id
 * @param {boolean} fromSpam Indicate if the deletion request comes from the spam folder.
 * @returns {object|null} The mail object if successfully deleted, null otherwise.
 */
function deleteMail(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return null;
    if (mail) {
        if (mail.to === email) {
            mail.deletedForReceiver = true;
        }
        if (mail.from === email) {
            mail.deletedForSender = true;
        }
        return mail;
    }
    const draft = draftMails.find(d => d.id === id && d.from === email);
    if (draft && !draft.deleted) {
        draft.deleted = true;
        return draft;
    }
    return null;
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
    if (mail) {
        if (mail.from === email) {
            if (!mail.labelsForSender.includes(labelId)) {
                mail.labelsForSender.push(labelId);
            }
        }
        if (mail.to === email) {
            if (!mail.labelsForReceiver.includes(labelId)) {
                mail.labelsForReceiver.push(labelId);
            }
        }
        return true;
    }
    const draft = draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (draft) {
        if (!draft.labels) draft.labels = [];
        if (!draft.labels.includes(labelId)) {
            draft.labels.push(labelId);
        }
        return true;
    }

    return false;
}

/**
 * Remove a label from a mail.
 * @param {string} email
 * @param {number} id
 * @param {string} labelId
 * @returns true if removed, false otherwise
 */
function removeLabel(email, id, labelId) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email));
    if (!mail) return false;
    if (mail) {
        if (mail.from === email) {
            mail.labelsForSender = mail.labelsForSender.filter(l => l !== labelId);
        }
        if (mail.to === email) {
            mail.labelsForReceiver = mail.labelsForReceiver.filter(l => l !== labelId);
        }
        return true;
    }
    const draft = draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (draft && draft.labels) {
        draft.labels = draft.labels.filter(l => l !== labelId);
        return true;
    }
    return false;
}

function getInbox(email) {
    return mails.filter(m => m.to === email && !m.deletedForReceiver && !m.isSpam);
}

function getSent(email) {
    return mails.filter(m => m.from === email && !m.deletedForSender);
}

function getSpam(email) {
    return mails.filter(m => m.to === email && m.isSpam && !m.deletedForReceiver);
}

function markAsSpam(mail, id) {
    if (!mail) return null;
    mail.isSpam = true;
    return mail;
}

function unmarkAsSpam(mail, id) {
    if (!mail) return null;
    mail.isSpam = false;
    return mail;
}


function getDeletedMails(email) {
    console.log('Filtering mails for deleted for:', email); // Add this
    console.log('Total mails in array:', mails.length); // Add this
    const filteredMails = mails.filter(m => (m.from === email && m.deletedForSender) || (m.to === email && m.deletedForReceiver));
    const filteredDrafts = draftMails.filter(d => d.from === email && d.deleted);
    const filtered = [...filteredMails, ...filteredDrafts];
    console.log('Filtered deleted mails count:', filtered.length); // Add this
    return filtered;
}

/**
 * Mark a mail as read.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function markAsRead(email, id) {
    const mail = mails.find(m => m.id === id && m.to === email);
    if (!mail) return null;
    mail.isRead = true;
    return mail;
}

/**
 * Mark a mail as unread.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function markAsUnread(email, id) {
    const mail = mails.find(m => m.id === id && m.to === email);
    if (!mail) return null;
    mail.isRead = false;
    return mail;
}

/**
 * Mark a mail as important.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function markAsImportant(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email))
        || draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (!mail) return null;
    mail.isImportant = true;
    return mail;
}

/**
 * Unmark a mail as important.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function unmarkAsImportant(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email)) 
        || draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (!mail) return null;
    mail.isImportant = false;
    return mail;
}

/**
 * Get all important mails for a user.
 * @param {string} email 
 * @return {Array} Array of important mails
 */
function getImportantMails(email) {
    const regularMails = mails.filter(
        m =>
            ((m.from === email && !m.deletedForSender) || (m.to === email && !m.deletedForReceiver)) &&
            m.isImportant
    );

    const importantDrafts = draftMails.filter(
        d => d.from === email && !d.deleted && d.isImportant
    );

    console.log('Regular mails found:', regularMails.length);
    console.log('Important drafts found:', importantDrafts.length);
    console.log('Total returned:', [...regularMails, ...importantDrafts].length);
    return [...regularMails, ...importantDrafts]
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 25);
}

/**
 * Mark a mail as starred.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function markAsStarred(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email))
        || draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (!mail) return null;
    mail.isStarred = true;
    return mail;
}

/**
 * Unmark a mail as starred.
 * @param {string} email
 * @param {number} id
 * @returns {object|null} The updated mail object if successful, null otherwise.
 */
function unmarkAsStarred(email, id) {
    const mail = mails.find(m => m.id === id && (m.from === email || m.to === email))
        || draftMails.find(d => d.id === id && d.from === email && !d.deleted);
    if (!mail) return null;
    mail.isStarred = false;
    return mail;
}

/**
 * Get all starred mails for a user.
 * @param {string} email 
 * @return {Array} Array of starred mails
 */
function getStarredMails(email) {
    const regularStarred = mails.filter(m => ((m.from === email && !m.deletedForSender) || (m.to === email && !m.deletedForReceiver)) && m.isStarred);
    const draftStarred = draftMails.filter(d => d.from === email && !d.deleted && d.isStarred);
    return [...regularStarred, ...draftStarred]
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 25);
}

module.exports = {
    getAll,
    getById,
    search,
    createDraft,
    createMail,
    updateDraft,
    deleteMail,
    addLabel,
    removeLabel,
    getDrafts,
    getInbox,
    getSent,
    getSpam,
    markAsSpam,
    unmarkAsSpam,
    getDeletedMails,
    markAsRead,
    markAsUnread,
    markAsImportant,
    unmarkAsImportant,
    getImportantMails,
    markAsStarred,
    unmarkAsStarred,
    getStarredMails,
};