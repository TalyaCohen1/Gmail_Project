const mailModel = require('../models/mailModel');
const userModel = require('../models/userModel');
const { checkUrl, addUrl, removeUrl, removeUrl_s, addUrl_s } = require('../services/blacklistService'); // Import deleteFromBlacklist
const URL_REGEX = /(?:https?:\/\/)?(?:www\.)?[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)+(?:\/\S*)?/gi;

/**
 * GET /api/mails
 */
exports.listMails = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const mails = mailModel.getAll(email);
    const mailsWithSenderInfo = mails.map(mail => {
        const senderUser = userModel.findByEmail(mail.from);
        if (senderUser) {
            return {
                ...mail,
                fromUser: {
                    fullName: senderUser.fullName,
                    email: senderUser.emailAddress,
                    profileImage: senderUser.profileImage || '/uploads/default-profile.png'
                }
            };
        }
        return mail;
    });
    res.status(200).json(mailsWithSenderInfo);
};

/**
 * GET /api/mails/:id
 */
exports.getMail = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    let mail = mailModel.getById(email, id);
    if (!mail) {
        mail = mailModel.getDraftById(email, id);
    }
    const senderUser = userModel.findByEmail(mail.from);
    if (senderUser) {
        mail.fromUser = {
            fullName: senderUser.fullName,
            email: senderUser.emailAddress,
            profileImage: senderUser.profileImage || '/uploads/default-profile.png'
        };
    }

    res.status(200).json(mail);
};

/**
 * GET /api/mails/search/:query
 */
exports.searchMails = (req, res) => {
    const query = req.params.query;
    const email = userModel.findById(req.userId).emailAddress;
    const results = mailModel.search(email, query);
    res.status(200).json(results);
};

/**
 * POST /api/mails
 */
exports.sendMail = async (req, res) => {
    try {
        const send = req.body.send;
        const fromId = req.userId;
        const fromUser = await userModel.findById(fromId);
        if (!fromUser) {
            return res.status(400).json({ error: 'Sender does not exist' });
        }
        const from = fromUser.emailAddress;
        let { to, subject = '', body = '', isImportant = false, isStarred = false, labels = []} = req.body;

        if (send != true) {
            const draft = mailModel.createDraft({ from, to, subject, body, isImportant, isStarred, labels });
            return res.status(201).json(draft);
        }

        if (!to) {
            return res.status(400).json({ error: 'Missing "to" field' });
        }

        const toUser = await userModel.findByEmail(to);
        if (!toUser) {
            return res.status(400).json({ error: 'Recipient email does not exist' });
        }

        const urls = body.match(URL_REGEX) || [];
        const urlsFromSubject = subject.match(URL_REGEX) || [];
        let isSpam = false; // Changed from isBlacklisted to isSpam

        for (const url of urls) {
            const blacklisted = await checkUrl(url);
            if (blacklisted) {
                isSpam = true;
                break; // No need to check further if one blacklisted URL is found
            }
        }
        if (!isSpam) { // Only check subject URLs if not already marked as spam
            for (const url of urlsFromSubject) {
                const blacklisted = await checkUrl(url);
                if (blacklisted) {
                    isSpam = true;
                    break;
                }
            }
        }
        const mail = await mailModel.createMail({ from, to, subject, body, isSpam, isImportant, isStarred, labels });


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
exports.updateDraft = async (req, res) => {
    const send = req.body.send;

    const id = Number(req.params.id);
    const fromId = req.userId;
    const fromUser = await userModel.findById(fromId);
    const from = fromUser.emailAddress;
    const { subject, body, to, isImportant, isStarred, labels } = req.body;

    if (subject === undefined && body === undefined && to === undefined) {
        return res.status(400).json({ error: 'No fields to update' });
    }

    if (send === true) {
        if (!to) {
            return res.status(400).json({ error: 'Missing "to" field' });
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
            return res.status(500).json({ error: 'Blacklist service error' });
        }
    }
    const updated = mailModel.updateDraft(from, id, { subject, body, to, send, isSpam, isImportant, isStarred, labels });

    if (!updated) {
        return res.status(404).json({ error: 'Draft not found or no permission' });
    }

    res.status(200).json(updated);
};

/**
 * DELETE /api/mails/:id
 * Added logic to remove blacklisted URLs if the mail is spam and being deleted from spam.
 */
exports.deleteMail = async (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;

    const mail = mailModel.getById(email, id);
    if (!mail) {
        return res.status(404).json({ error: 'Mail not found' });
    }

    const removedMail = mailModel.deleteMail(email, id); // Pass fromSpam to the model
    if (!removedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.sendStatus(204);
};

/**
 * POST /api/mails/:id/labels
 */
exports.addLabel = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const { label } = req.body;
    const updated = mailModel.addLabel(email, id, label);
    if (!updated) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.status(201).json(updated);
};
/**
 * DELETE /api/mails/:id/labels/:labelId
 */
exports.removeLabel = (req, res) => {
    const id = Number(req.params.id);
    const labelId = Number(req.params.labelId);
    const email = userModel.findById(req.userId).emailAddress;
    const updated = mailModel.removeLabel(email, id, labelId);
    if (!updated) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.sendStatus(204);
};

/**
 * GET /api/mails/:id/labels
 */
exports.getLabels = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const labels = mailModel.getLabels(email, id);
    if (!labels) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    res.status(200).json(labels);
};

/**
 * GET /api/mails/drafts
 */
exports.getDrafts = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const drafts = mailModel.getDrafts(email);
    res.status(200).json(drafts);
};

/**
 * GET /api/mails/inbox
 */
exports.getInbox = (req, res) => {
    const userId = req.userId; // Get the userId from the request object
    // Ensure the userId is set in the request object
    const user = userModel.findById(userId);
    if (!user) {
    return res.status(404).json({ error: "User not found" });
    }
    const email = user.emailAddress;
    const inbox = mailModel.getInbox(email);
    res.status(200).json(inbox);
};

/**
 * GET /api/mails/sent
 */
exports.getSent = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const sent = mailModel.getSent(email);
    res.status(200).json(sent);
};

/**
 * GET /api/mails/spam
 */
exports.getSpamMails = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const spamMails = mailModel.getSpam(email);
    res.status(200).json(spamMails);
};

/**
 * POST /api/mails/:id/spam (To mark as spam)
 */
exports.markMailAsSpam = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const mail = mailModel.getById(email, id);
    if (!mail) {
        return res.status(404).json({ error: 'Mail not found' });
    }
    const subjectUrls = mail.subject.match(URL_REGEX) || [];
    const bodyUrls = mail.body.match(URL_REGEX) || [];
    const allUrls = [...subjectUrls, ...bodyUrls];

    for (const url of allUrls) {
        try {
            addUrl_s(url);
        } catch (e) {
            console.error(`Error adding URL to blacklist: ${url}`, e);
        }
    }

    const updatedMail = mailModel.markAsSpam(mail, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
};


/**
 * DELETE /api/mails/:id/spam (To unmark as spam)
 */
exports.unmarkMailAsSpam = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const mail = mailModel.getById(email, id);
    if (!mail) {
        return res.status(404).json({ error: 'Mail not found' });
    }

    const subjectUrls = mail.subject.match(URL_REGEX) || [];
    const bodyUrls = mail.body.match(URL_REGEX) || [];
    const allUrls = [...subjectUrls, ...bodyUrls];

    for (const url of allUrls) {
        try {
            removeUrl_s(url);
        } catch (e) {
            console.error(`Error removing URL from blacklist: ${url}`, e);
        }
    }

    const updatedMail = mailModel.unmarkAsSpam(mail, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
};


exports.getDeletedMails = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const deletedMails = mailModel.getDeletedMails(email);
    res.status(200).json(deletedMails);
};

/**
 * POST /api/mails/:id/read
 */
exports.markAsRead = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.markAsRead(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
}

/**
 * POST /api/mails/:id/unread
 */
exports.markAsUnread = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.markAsUnread(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
}

/**
 * POST /api/mails/:id/important
 */
exports.markMailAsImportant = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.markAsImportant(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
};
/**
 * DELETE /api/mails/:id/important
 */
exports.unmarkMailAsImportant = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.unmarkAsImportant(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
};
/**
 * GET /api/mails/important
 */
exports.getImportantMails = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const importantMails = mailModel.getImportantMails(email);
    res.status(200).json(importantMails);
};
/**
 * POST /api/mails/:id/star
 */
exports.markMailAsStarred = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.markAsStarred(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
}
/**
 * DELETE /api/mails/:id/star
 */
exports.unmarkMailAsStarred = (req, res) => {
    const id = Number(req.params.id);
    const email = userModel.findById(req.userId).emailAddress;
    const updatedMail = mailModel.unmarkAsStarred(email, id);
    if (!updatedMail) {
        return res.status(404).json({ error: 'Mail not found or no permission' });
    }
    res.status(200).json(updatedMail);
}
/**
 * GET /api/mails/starred
 */
exports.getStarredMails = (req, res) => {
    const email = userModel.findById(req.userId).emailAddress;
    const starredMails = mailModel.getStarredMails(email);
    res.status(200).json(starredMails);
}