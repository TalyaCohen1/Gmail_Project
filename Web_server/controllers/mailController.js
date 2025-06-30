const mailModel = require('../models/mailModel');
const userModel = require('../models/userModel');
const { checkUrl, addUrl, removeUrl, removeUrl_s, addUrl_s } = require('../services/blacklistService');
const URL_REGEX = /(?:https?:\/\/)?(?:www\.)?[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)+(?:\/\S*)?/gi;

/**
 * Helper function to format mail objects for client response.
 * Includes `id` property for frontend compatibility.
 * @param {Object} mail - Mongoose mail document.
 * @param {Object|null} senderUser - User document of the sender, if available.
 * @returns {Object} Formatted mail object with `id` and `fromUser` info.
 */
const formatMailForResponse = (mail, senderUser = null) => {
    // mail.toObject() converts the Mongoose document to a plain JavaScript object,
    // which is safer to modify and send as JSON.
    const formattedMail = mail.toObject();
    formattedMail.id = formattedMail._id; // Add 'id' property for frontend compatibility

    if (senderUser) {
        formattedMail.fromUser = {
            fullName: senderUser.fullName,
            email: senderUser.emailAddress,
            profileImage: senderUser.profileImage || '/uploads/default-profile.png'
        };
    }
    return formattedMail;
};

/**
 * Helper function to retrieve user email from req.userId.
 * @param {Object} req - Express request object.
 * @returns {Promise<string|null>} User email address or null if not found.
 */
async function getUserEmail(req) {
    const user = await userModel.findById(req.userId);
    return user ? user.emailAddress : null;
}

/**
 * GET /api/mails
 * List up to 25 most recent mails for the authenticated user.
 */
exports.listMails = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const mails = await mailModel.getAll(email);
        const mailsWithSenderInfo = await Promise.all(mails.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(mailsWithSenderInfo);
    } catch (error) {
        console.error('Error listing mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/:id
 * Retrieve a single mail by ID for the authenticated user.
 */
exports.getMail = async (req, res) => {
    try {
        const id = req.params.id; // Mail ID is now a string (MongoDB ObjectId)
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const mail = await mailModel.getById(email, id);

        if (!mail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }

        const senderUser = await userModel.findByEmail(mail.from);
        res.status(200).json(formatMailForResponse(mail, senderUser));
    } catch (error) {
        console.error('Error getting mail by ID:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/search/:query
 * Search mails by query string in subject or body for the authenticated user.
 */
exports.searchMails = async (req, res) => {
    try {
        const query = req.params.query;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const results = await mailModel.search(email, query);
        const formattedResults = await Promise.all(results.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedResults);
    } catch (error) {
        console.error('Error searching mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails
 * Create a new mail (sent or draft) with blacklist check.
 */
exports.sendMail = async (req, res) => {
    try {
        const send = req.body.send;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'Sender user not found' });
        }
        const fromId = findByEmail(email)._id; // Get the authenticated user's ID

        let { to, subject = '', body = '', isImportant = false, isStarred = false, labels = [], id } = req.body;

        // If it's a draft (send is not explicitly true)
        if (send !== true) {
            const draft = await mailModel.createDraft({ from: email, to, subject, body, isImportant, isStarred, labels });
            res.status(201).json(formatMailForResponse(draft));
            return;
        }

        // If sending the mail (send === true)
        if (!to) {
            return res.status(400).json({ error: 'Missing "to" field for sending mail' });
        }

        const toUser = await userModel.findByEmail(to);
        if (!toUser) {
            return res.status(400).json({ error: 'Recipient email does not exist' });
        }

        const urls = body.match(URL_REGEX) || [];
        const urlsFromSubject = subject.match(URL_REGEX) || [];
        let isSpam = false;

        for (const url of urls) {
            const blacklisted = await checkUrl(url);
            if (blacklisted) {
                isSpam = true;
                break;
            }
        }
        if (!isSpam) {
            for (const url of urlsFromSubject) {
                const blacklisted = await checkUrl(url);
                if (blacklisted) {
                    isSpam = true;
                    break;
                }
            }
        }
        // If an ID is provided, it means we are sending an existing draft.
        // The createMail function will handle updating the draft or creating new.
        const mail = await mailModel.createMail({ from: email, fromId, to, subject, body, id, isSpam, isImportant, isStarred, labels });

        res.status(201)
            .location(`/api/mails/${mail._id}`)
            .json(formatMailForResponse(mail));

    } catch (error) {
        console.error('Error sending mail:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * PATCH /api/mails/:id
 * Update an existing draft's subject/body, or send it as a new mail.
 */
exports.updateDraft = async (req, res) => {
    try {
        const send = req.body.send;
        const id = req.params.id; // Mail ID is string
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const { subject, body, to, isImportant, isStarred, labels } = req.body;

        if (subject === undefined && body === undefined && to === undefined && isImportant === undefined && isStarred === undefined && labels === undefined && send === undefined) {
            return res.status(400).json({ error: 'No fields provided for update' });
        }

        // If trying to send a draft, validate 'to' field
        if (send === true) {
            if (!to) {
                return res.status(400).json({ error: 'Missing "to" field for sending mail' });
            }
            const toUser = await userModel.findByEmail(to);
            if (!toUser) {
                return res.status(400).json({ error: 'Recipient email does not exist' });
            }
        }

        const subjectUrls = subject ? subject.match(URL_REGEX) || [] : [];
        const bodyUrls = body ? body.match(URL_REGEX) || [] : [];
        const allUrls = [...subjectUrls, ...bodyUrls];
        let isSpam = false;

        for (const url of allUrls) {
            try {
                const blacklisted = await checkUrl(url);
                if (blacklisted) {
                    isSpam = true;
                    break;
                }
            } catch (e) {
                console.error('Blacklist service error:', e);
                return res.status(500).json({ error: 'Blacklist service error' });
            }
        }
        // Pass isSpam along for draft conversion logic
        const updatedMail = await mailModel.updateDraft(email, id, { subject, body, to, send, isSpam, isImportant, isStarred, labels });

        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail (draft) not found or no permission to update' });
        }

        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error updating draft:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * DELETE /api/mails/:id
 * Soft delete a mail for the authenticated user.
 */
exports.deleteMail = async (req, res) => {
    try {
        const id = req.params.id; // Mail ID is string
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const removedMail = await mailModel.deleteMail(email, id);
        if (!removedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission to delete' });
        }
        res.sendStatus(204); // No content on successful delete
    } catch (error) {
        console.error('Error deleting mail:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/labels
 * Add a label to a mail for the authenticated user.
 */
exports.addLabel = async (req, res) => {
    try {
        const id = req.params.id; // Mail ID (string)
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }
        const { label } = req.body;

        if (!label) {
            return res.status(400).json({ error: 'Label ID is required' });
        }

        const updatedMail = await mailModel.addLabel(email, id, label);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or failed to add label' });
        }
        res.status(200).json(formatMailForResponse(updatedMail)); // Return 200 OK and updated mail
    } catch (error) {
        console.error('Error adding label to mail:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * DELETE /api/mails/:id/labels/:labelId
 * Remove a label from a mail for the authenticated user.
 */
exports.removeLabel = async (req, res) => {
    try {
        const id = req.params.id; // Mail ID (string)
        const labelId = req.params.labelId; // Label ID (string)
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.removeLabel(email, id, labelId);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or failed to remove label' });
        }
        res.sendStatus(204); // No content on successful removal
    } catch (error) {
        console.error('Error removing label from mail:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/:id/labels
 * Get labels associated with a mail for the authenticated user.
 */
exports.getLabels = async (req, res) => {
    try {
        const id = req.params.id; // Mail ID (string)
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const labels = await mailModel.getLabels(email, id);
        // mailModel.getLabels returns an array of label IDs. We don't need to format them.
        if (labels === null) { // Check for null (mail not found/no permission)
            return res.status(404).json({ error: 'Mail not found or no labels associated for this user' });
        }
        res.status(200).json(labels);
    } catch (error) {
        console.error('Error getting labels for mail:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/drafts
 * Get all drafts for the authenticated user.
 */
exports.getDrafts = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const drafts = await mailModel.getDrafts(email);
        const formattedDrafts = await Promise.all(drafts.map(async draft => {
            const senderUser = await userModel.findByEmail(draft.from); // Draft's sender is always the current user
            return formatMailForResponse(draft, senderUser);
        }));
        res.status(200).json(formattedDrafts);
    } catch (error) {
        console.error('Error getting drafts:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/inbox
 * Get all mails in the inbox for the authenticated user.
 */
exports.getInbox = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: "User not found" });
        }
        const inbox = await mailModel.getInbox(email);
        const formattedInbox = await Promise.all(inbox.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedInbox);
    } catch (error) {
        console.error('Error getting inbox:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/sent
 * Get all mails sent by the authenticated user.
 */
exports.getSent = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const sent = await mailModel.getSent(email);
        const formattedSent = await Promise.all(sent.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from); // Sender is always the current user for sent mails
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedSent);
    } catch (error) {
        console.error('Error getting sent mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/spam
 * Get all spam mails for the authenticated user.
 */
exports.getSpamMails = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const spamMails = await mailModel.getSpam(email);
        const formattedSpamMails = await Promise.all(spamMails.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedSpamMails);
    } catch (error) {
        console.error('Error getting spam mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/spam
 * Mark a mail as spam for the authenticated user.
 */
exports.markMailAsSpam = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const mail = await mailModel.getById(email, id);
        if (!mail) {
            return res.status(404).json({ error: 'Mail not found' });
        }

        const subjectUrls = mail.subject.match(URL_REGEX) || [];
        const bodyUrls = mail.body.match(URL_REGEX) || [];
        const allUrls = [...subjectUrls, ...bodyUrls];

        for (const url of allUrls) {
            try {
                // Ensure addUrl_s is asynchronous if it performs network/DB operations
                addUrl_s(url);
            } catch (e) {
                console.error(`Error adding URL to blacklist: ${url}`, e);
            }
        }

        const updatedMail = await mailModel.markAsSpam(mail, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error marking mail as spam:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * DELETE /api/mails/:id/spam
 * Unmark a mail as spam for the authenticated user.
 */
exports.unmarkMailAsSpam = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const mail = await mailModel.getById(email, id);
        if (!mail) {
            return res.status(404).json({ error: 'Mail not found' });
        }

        const subjectUrls = mail.subject.match(URL_REGEX) || [];
        const bodyUrls = mail.body.match(URL_REGEX) || [];
        const allUrls = [...subjectUrls, ...bodyUrls];

        for (const url of allUrls) {
            try {
                // Ensure removeUrl_s is asynchronous if it performs network/DB operations
                removeUrl_s(url);
            } catch (e) {
                console.error(`Error removing URL from blacklist: ${url}`, e);
            }
        }

        const updatedMail = await mailModel.unmarkAsSpam(mail, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error unmarking mail as spam:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/deleted
 * Get all deleted mails for the authenticated user.
 */
exports.getDeletedMails = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const deletedMails = await mailModel.getDeletedMails(email);
        const formattedDeletedMails = await Promise.all(deletedMails.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedDeletedMails);
    } catch (error) {
        console.error('Error getting deleted mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/read
 * Mark a mail as read for the authenticated user.
 */
exports.markAsRead = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.markAsRead(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error marking mail as read:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/unread
 * Mark a mail as unread for the authenticated user.
 */
exports.markAsUnread = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.markAsUnread(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error marking mail as unread:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/important
 * Mark a mail as important for the authenticated user.
 */
exports.markMailAsImportant = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.markAsImportant(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error marking mail as important:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * DELETE /api/mails/:id/important
 * Unmark a mail as important for the authenticated user.
 */
exports.unmarkMailAsImportant = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.unmarkAsImportant(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error unmarking mail as important:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/important
 * Get all important mails for the authenticated user.
 */
exports.getImportantMails = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const importantMails = await mailModel.getImportantMails(email);
        const formattedImportantMails = await Promise.all(importantMails.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedImportantMails);
    } catch (error) {
        console.error('Error getting important mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * POST /api/mails/:id/star
 * Mark a mail as starred for the authenticated user.
 */
exports.markMailAsStarred = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.markAsStarred(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error marking mail as starred:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * DELETE /api/mails/:id/star
 * Unmark a mail as starred for the authenticated user.
 */
exports.unmarkMailAsStarred = async (req, res) => {
    try {
        const id = req.params.id;
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const updatedMail = await mailModel.unmarkAsStarred(email, id);
        if (!updatedMail) {
            return res.status(404).json({ error: 'Mail not found or no permission' });
        }
        res.status(200).json(formatMailForResponse(updatedMail));
    } catch (error) {
        console.error('Error unmarking mail as starred:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * GET /api/mails/starred
 * Get all starred mails for the authenticated user.
 */
exports.getStarredMails = async (req, res) => {
    try {
        const email = await getUserEmail(req);
        if (!email) {
            return res.status(404).json({ error: 'User not found' });
        }

        const starredMails = await mailModel.getStarredMails(email);
        const formattedStarredMails = await Promise.all(starredMails.map(async mail => {
            const senderUser = await userModel.findByEmail(mail.from);
            return formatMailForResponse(mail, senderUser);
        }));
        res.status(200).json(formattedStarredMails);
    } catch (error) {
        console.error('Error getting starred mails:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
