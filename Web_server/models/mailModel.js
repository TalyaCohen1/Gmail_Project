const mongoose = require('mongoose');

// Define the Mail Schema
const mailSchema = new mongoose.Schema({
    from: {
        type: String,
        required: true,
        trim: true
    },
    fromId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User', // Crucial for populating sender's details
        required: true // Assuming every mail has a sender user ID
    },
    to: {
        type: String,
        trim: true,
        // You might want to add validation for email format here if not already handled by userModel
    },
    subject: {
        type: String,
        default: ''
    },
    body: {
        type: String,
        default: ''
    },
    date: {
        type: String, // Storing as ISO string (e.g., "2023-10-27T10:00:00.000Z")
        default: () => new Date().toISOString()
    },
    timestamp: {
        type: Number, // Unix timestamp for easy sorting (milliseconds since epoch)
        default: () => Date.now()
    },
    send: { // True for sent/received mails, false for drafts
        type: Boolean,
        default: false
    },
    isSpam: {
        type: Boolean,
        default: false
    },
    isRead: { // Primarily relevant for recipient's view
        type: Boolean,
        default: false
    },
    isImportant: {
        type: Boolean,
        default: false
    },
    isStarred: {
        type: Boolean,
        default: false
    },
    // Flags for soft deletion, specific to sender/receiver
    deletedForSender: {
        type: Boolean,
        default: false
    },
    deletedForReceiver: {
        type: Boolean,
        default: false
    },
    // References to Label IDs. These will be populated based on the user (sender/receiver)
    // The previous logic used labelsForSender/labelsForReceiver directly on the mail object.
    // This is valid, but the Mail model is effectively storing cross-user data (who applied which label).
    // An alternative (more normalized) would be to have labels stored only in the Label model,
    // which references mail IDs. However, to maintain the logic of your existing addLabel/removeLabel
    // functions in mailModel and how they modify mail properties, we'll keep these arrays here.
    labelsForSender: [{
        type: mongoose.Schema.Types.ObjectId, // Assuming Label _id is ObjectId
        ref: 'Label'
    }],
    labelsForReceiver: [{
        type: mongoose.Schema.Types.ObjectId, // Assuming Label _id is ObjectId
        ref: 'Label'
    }]
}, {
    timestamps: true // Adds createdAt and updatedAt fields automatically
});

// Create the Mail Model
const Mail = mongoose.model('Mail', mailSchema);


/**
 * Returns up to 25 most recent mails for this user (sent or received), not spam, not deleted.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of mail objects.
 */
async function getAll(email) {
    const mails = await Mail.find({
        $or: [
            { from: email, deletedForSender: false }, // Mails sent by user, not deleted by sender
            { to: email, deletedForReceiver: false }  // Mails received by user, not deleted by receiver
        ],
        isSpam: false, // Not marked as spam
        send: true     // Only consider sent mails (not drafts)
    })
    .sort({ timestamp: -1 }) // Sort by most recent first
    .limit(25) // Limit to 25 mails
    .exec(); // Execute the query
    return mails;
}

/**
 * Find one mail by ID for this user; null if not found or not theirs.
 * This function also handles retrieving drafts if the mail is not found in sent/received.
 * @param {string} userEmail - The user's email.
 * @param {string} mailId - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the mail object or null.
 */
async function getById(userEmail, mailId) {
    if (!mongoose.Types.ObjectId.isValid(mailId)) {
        return null; // Ensure it's a valid MongoDB ObjectId
    }

    // Try to find in sent/received mails or drafts by this user
    let mail = await Mail.findOne({
        _id: mailId,
        $or: [
            // Mail is sent by user and not deleted by sender
            { from: userEmail, deletedForSender: false },
            // Mail is received by user and not deleted by receiver, and it's a sent mail
            { to: userEmail, deletedForReceiver: false, send: true },
            // Mail is a draft by this user (not yet sent, not deleted by sender)
            { from: userEmail, send: false, deletedForSender: false }
        ]
    }).exec();
    return mail;
}

/**
 * Find one draft mail by ID for this user; null if not found or not theirs.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the draft mail.
 * @returns {Promise<Object|null>} Promise resolving to the draft mail object or null.
 */
async function getDraftById(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const draft = await Mail.findOne({
        _id: id,
        from: email,
        send: false, // Ensure it's a draft
        deletedForSender: false // Drafts are deleted when sender marks them as deleted
    }).exec();
    return draft;
}

/**
 * Search this user’s mails for query in subject or body.
 * @param {string} email - The user's email.
 * @param {string} query - The search query string.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of matching mail objects.
 */
async function search(email, query) {
    const ql = query.toLowerCase();
    const mails = await Mail.find({
        $or: [
            { from: email }, // Mails sent by user
            { to: email }    // Mails received by user
        ],
        isSpam: false, // Not spam
        send: true,    // Only search in sent/received mails, not drafts
        $and: [
            {
                $or: [
                    { subject: { $regex: ql, $options: 'i' } }, // Case-insensitive regex search in subject
                    { body: { $regex: ql, $options: 'i' } }      // Case-insensitive regex search in body
                ]
            }
        ]
    }).exec();
    return mails;
}

/**
 * Create a new draft mail record.
 * @param {object} mailData - Object containing from, to, subject, body, isImportant, isStarred, labels.
 * @returns {Promise<Object>} Promise resolving to the newly created draft mail object.
 */
async function createDraft({ from, fromId, to = '', subject = '', body = '', isImportant = false, isStarred = false, labels = [] }) {
    const newDraft = new Mail({
        from,
        fromId,
        to,
        subject,
        body,
        send: false, // Mark as draft
        isImportant,
        isStarred,
        labelsForSender: labels, // Labels apply to sender for drafts
        labelsForReceiver: [],   // No receiver yet for drafts
        deletedForSender: false // Not deleted when created
    });
    await newDraft.save();
    return newDraft;
}

/**
 * Create a new mail record (sent mail).
 * This function can also convert an existing draft into a sent mail.
 * @param {object} mailData - Object containing from, to, subject, body, id (optional draft _id), isSpam, isImportant, isStarred, labels.
 * @returns {Promise<Object>} Promise resolving to the newly created or updated mail object.
 */
async function createMail({ from, fromId, to, subject = '', body = '', id, isSpam = false, isImportant = false, isStarred = false, labels = [] }) {
    let mail;

    if (id && mongoose.Types.ObjectId.isValid(id)) {
        // If an ID is provided, try to find an existing draft to convert
        mail = await Mail.findOne({ _id: id, from: from, send: false, deletedForSender: false }).exec();
        if (mail) {
            // Update draft properties to convert it to a sent mail
            mail.to = to;
            mail.subject = subject;
            mail.body = body;
            mail.send = true; // Mark as sent
            mail.isSpam = isSpam;
            mail.isImportant = isImportant;
            mail.isStarred = isStarred;
            mail.labelsForSender = labels; // Copy labels from draft to sent mail for sender
            mail.labelsForReceiver = []; // Reset receiver labels
            mail.timestamp = Date.now(); // Update timestamp to current send time
            mail.date = new Date().toISOString();
            mail.isRead = false; // Sent mail is unread for recipient
            mail.deletedForReceiver = false; // Ensure receiver's deletion flag is false on send
            mail.deletedForSender = false; // Ensure sender's deletion flag is false on send (if it was somehow set)
        }
    }

    if (!mail) {
        // If no valid ID was provided, or draft was not found/not eligible, create a new mail
        mail = new Mail({
            from,
            fromId, 
            to,
            subject,
            body,
            send: true, // Mark as sent
            isSpam,
            isImportant,
            isStarred,
            labelsForSender: labels,
            labelsForReceiver: [], // Receiver starts with no labels
            isRead: false, // New mail is unread for receiver
            deletedForSender: false,
            deletedForReceiver: false
        });
    }

    await mail.save();
    return mail;
}

/**
 * Update an existing draft's subject/body, or send it as a new mail.
 * Returns updated mail/sent mail object or null.
 * @param {string} email - The sender's email.
 * @param {string} id - The MongoDB _id of the draft mail.
 * @param {object} fields - Fields to update: subject, body, to, send, isSpam, isImportant, isStarred, labels.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object or null.
 */
async function updateDraft(email, id, fields) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }

    const draft = await Mail.findOne({ _id: id, from: email, send: false, deletedForSender: false }).exec();
    if (!draft) return null;

    if (fields.subject !== undefined) draft.subject = fields.subject;
    if (fields.body !== undefined) draft.body = fields.body;
    if (fields.to !== undefined) draft.to = fields.to;
    if (fields.isImportant !== undefined) draft.isImportant = fields.isImportant;
    if (fields.isStarred !== undefined) draft.isStarred = fields.isStarred;
    if (fields.labels !== undefined) draft.labelsForSender = fields.labels; // Update labels for sender

    if (fields.send === true) {
        // If 'send' is true, convert draft to a sent mail
        draft.send = true;
        draft.isSpam = fields.isSpam; // Apply spam status from send logic
        draft.timestamp = Date.now(); // Update timestamp to reflect send time
        draft.date = new Date().toISOString(); // Update date
        draft.isRead = false; // Sent mail is unread for recipient
        draft.deletedForReceiver = false; // Ensure recipient's deletion flag is false on send
        draft.deletedForSender = false; // Ensure sender's deletion flag is false (if it was marked deleted as draft)
    }

    await draft.save();
    return draft;
}


/**
 * Delete a mail (soft delete), only if email is the email of the sender or recipient.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the deleted mail object if successful, null otherwise.
 */
async function deleteMail(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }

    const mail = await Mail.findById(id).exec();
    if (!mail) return null; // Mail not found

    let isUpdated = false;
    if (mail.to === email) {
        mail.deletedForReceiver = true;
        isUpdated = true;
    }
    if (mail.from === email) {
        mail.deletedForSender = true;
        isUpdated = true;
    }

    if (isUpdated) {
        await mail.save();
        return mail;
    }
    return null; // Mail not related to this user
}

/**
 * Add a label to a mail for the relevant user (sender or receiver).
 * This function updates the labels directly on the mail document.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @param {string} labelId - The MongoDB _id of the label to add.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object or null.
 */
async function addLabel(email, id, labelId) {
    if (!mongoose.Types.ObjectId.isValid(id) || !mongoose.Types.ObjectId.isValid(labelId)) {
        return null;
    }

    const mail = await Mail.findById(id).exec();
    if (!mail) return null;

    // Check if the mail belongs to the user (as sender or receiver)
    let updated = false;
    if (mail.from === email) {
        if (!mail.labelsForSender.includes(labelId)) {
            mail.labelsForSender.push(labelId);
            updated = true;
        }
    } else if (mail.to === email) {
        if (!mail.labelsForReceiver.includes(labelId)) {
            mail.labelsForReceiver.push(labelId);
            updated = true;
        }
    }

    if (updated) {
        await mail.save();
        return mail;
    }
    return null; // Mail doesn't belong to this user or label already exists
}

/**
 * Remove a label from a mail for the relevant user (sender or receiver).
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @param {string} labelId - The MongoDB _id of the label to remove.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object or null.
 */
async function removeLabel(email, id, labelId) {
    if (!mongoose.Types.ObjectId.isValid(id) || !mongoose.Types.ObjectId.isValid(labelId)) {
        return null;
    }

    const mail = await Mail.findById(id).exec();
    if (!mail) return null;

    let updated = false;
    if (mail.from === email) {
        const initialLength = mail.labelsForSender.length;
        mail.labelsForSender = mail.labelsForSender.filter(l => l.toString() !== labelId.toString());
        if (mail.labelsForSender.length < initialLength) {
            updated = true;
        }
    } else if (mail.to === email) {
        const initialLength = mail.labelsForReceiver.length;
        mail.labelsForReceiver = mail.labelsForReceiver.filter(l => l.toString() !== labelId.toString());
        if (mail.labelsForReceiver.length < initialLength) {
            updated = true;
        }
    }

    if (updated) {
        await mail.save();
        return mail;
    }
    return null; // Mail doesn't belong to this user or label not found
}

/**
 * Get labels associated with a mail for a specific user.
 * This is based on whether the user is the sender or receiver.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Array<string>|null>} Promise resolving to an array of label IDs (strings) or null.
 */
async function getLabels(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }

    const mail = await Mail.findById(id).exec();
    if (!mail) return null;

    if (mail.from === email) {
        return mail.labelsForSender;
    } else if (mail.to === email) {
        return mail.labelsForReceiver;
    }
    return null; // Mail doesn't belong to this user
}

/**
 * Get all drafts for a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of draft mail objects.
 */
async function getDrafts(email) {
    return await Mail.find({ from: email, send: false, deletedForSender: false })
        .sort({ timestamp: -1 })
        .exec();
}

/**
 * Get all mails in the inbox for a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of inbox mail objects.
 */
async function getInbox(email) {
    return await Mail.find({
        to: email,
        deletedForReceiver: false,
        isSpam: false,
        send: true // Only received (sent) mails
    })
    .sort({ timestamp: -1 })
    .limit(25)
    .exec();
}

/**
 * Get all mails sent by a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of sent mail objects.
 */
async function getSent(email) {
    return await Mail.find({
        from: email,
        deletedForSender: false,
        send: true // Only sent mails
    })
    .sort({ timestamp: -1 })
    .limit(25)
    .exec();
}

/**
 * Get all spam mails for a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of spam mail objects.
 */
async function getSpam(email) {
    return await Mail.find({
        to: email, // Spam mails are always received mails
        isSpam: true,
        deletedForReceiver: false,
        send: true
    })
    .sort({ timestamp: -1 })
    .limit(25)
    .exec();
}

/**
 * Mark a mail as spam.
 * @param {Object} mail - The mail document object.
 * @param {string} id - The MongoDB _id of the mail (for consistency, though not used here).
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function markAsSpam(mail, id) { // mail object is passed directly
    if (!mail) return null;
    mail.isSpam = true;
    await mail.save();
    return mail;
}

/**
 * Unmark a mail as spam.
 * @param {Object} mail - The mail document object.
 * @param {string} id - The MongoDB _id of the mail (for consistency, though not used here).
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function unmarkAsSpam(mail, id) { // mail object is passed directly
    if (!mail) return null;
    mail.isSpam = false;
    await mail.save();
    return mail;
}

/**
 * Get all deleted mails for a user, including drafts.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of deleted mail objects.
 */
async function getDeletedMails(email) {
    const deletedMails = await Mail.find({
        $or: [
            { from: email, deletedForSender: true },   // Mails sent by user and deleted by sender
            { to: email, deletedForReceiver: true }    // Mails received by user and deleted by receiver
        ]
    })
    .sort({ timestamp: -1 })
    .exec();
    return deletedMails;
}

/**
 * Mark a mail as read.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function markAsRead(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    // Only recipient can mark as read
    const mail = await Mail.findOne({ _id: id, to: email, send: true }).exec();
    if (!mail) return null;
    mail.isRead = true;
    await mail.save();
    return mail;
}

/**
 * Mark a mail as unread.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function markAsUnread(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    // Only recipient can mark as unread
    const mail = await Mail.findOne({ _id: id, to: email, send: true }).exec();
    if (!mail) return null;
    mail.isRead = false;
    await mail.save();
    return mail;
}

/**
 * Mark a mail as important.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function markAsImportant(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const mail = await Mail.findOne({
        _id: id,
        $or: [
            { from: email }, // Sender can mark their mail important (including drafts)
            { to: email, send: true } // Recipient can mark sent mail important
        ]
    }).exec();

    if (!mail) return null;
    mail.isImportant = true;
    await mail.save();
    return mail;
}

/**
 * Unmark a mail as important.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function unmarkAsImportant(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const mail = await Mail.findOne({
        _id: id,
        $or: [
            { from: email },
            { to: email, send: true }
        ]
    }).exec();

    if (!mail) return null;
    mail.isImportant = false;
    await mail.save();
    return mail;
}

/**
 * Get all important mails for a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of important mail objects.
 */
async function getImportantMails(email) {
    return await Mail.find({
        $or: [
            { from: email, deletedForSender: false },
            { to: email, deletedForReceiver: false }
        ],
        isImportant: true
    })
    .sort({ timestamp: -1 })
    .limit(25)
    .exec();
}

/**
 * Mark a mail as starred.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function markAsStarred(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const mail = await Mail.findOne({
        _id: id,
        $or: [
            { from: email },
            { to: email, send: true }
        ]
    }).exec();

    if (!mail) return null;
    mail.isStarred = true;
    await mail.save();
    return mail;
}

/**
 * Unmark a mail as starred.
 * @param {string} email - The user's email.
 * @param {string} id - The MongoDB _id of the mail.
 * @returns {Promise<Object|null>} Promise resolving to the updated mail object if successful, null otherwise.
 */
async function unmarkAsStarred(email, id) {
    if (!mongoose.Types.ObjectId.isValid(id)) {
        return null;
    }
    const mail = await Mail.findOne({
        _id: id,
        $or: [
            { from: email },
            { to: email, send: true }
        ]
    }).exec();

    if (!mail) return null;
    mail.isStarred = false;
    await mail.save();
    return mail;
}

/**
 * Get all starred mails for a user.
 * @param {string} email - The user's email.
 * @returns {Promise<Array<Object>>} Promise resolving to an array of starred mail objects.
 */
async function getStarredMails(email) {
    return await Mail.find({
        $or: [
            { from: email, deletedForSender: false },
            { to: email, deletedForReceiver: false }
        ],
        isStarred: true
    })
    .sort({ timestamp: -1 })
    .limit(25)
    .exec();
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
    getDraftById
};
