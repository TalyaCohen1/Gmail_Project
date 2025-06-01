const net = require('net');

/**
 * Sends a plain command string to the blacklist server over TCP.
 * Resolves with the server's full response as a string.
 *
 * @param {string} commandStr - The command string to send (e.g., "GET url", "POST url").
 * @returns {Promise<string>} - Resolves to the server's response string.
 */
function sendCommandString(commandStr) {
    return new Promise((resolve, reject) => {
        const client = new net.Socket();
        let responseData = '';

        const host = 'blacklist_server'; // Docker service name
        const port = 12345;

        client.connect(port, host, () => {
            client.write(commandStr + '\n');
        });

        client.on('data', (data) => {
            responseData += data.toString();
            client.end();
        });

        client.on('end', () => {
            resolve(responseData);
        });

        client.on('error', (err) => {
            reject(err);
        });

        client.setTimeout(3000, () => {
            client.destroy();
            reject(new Error('Socket timeout'));
        });
    });
}

/**
 * Checks if a URL is blacklisted based on server response flags.
 *
 * @param {string} url - The URL to check.
 * @returns {Promise<boolean>} - True if the URL is blacklisted; otherwise false.
 */
async function checkUrl(url) {
    const response = await sendCommandString(`GET ${url}`);
    const parts = response.split('\n');
    const statusLine = (parts[0] || '').trim();
    const flagsLine = (parts[2] || '').trim();
    const flags = flagsLine.split(/\s+/);

    return (
        statusLine === '200 Ok' &&
        flags.length >= 2 &&
        flags[0] === 'true' &&
        flags[1] === 'true'
    );
}

/**
 * Express handler to add a URL to the blacklist.
 *
 * @param {Request} req - Express request containing `url` in the body.
 * @param {Response} res - Express response object.
 */
exports.addUrl = async (req, res) => {
    const url = req.body.url;
    if (!url) return res.status(400).json({ error: 'URL is required' });

    try {
        const response = await sendCommandString(`POST ${url}`);
        const statusLine = response.trim().split('\n')[0];
        if (statusLine.startsWith('201')) {
            res.status(201).send(statusLine);
        } else {
            res.status(400).send(statusLine);
        }
    } catch (err) {
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Express handler to remove a URL from the blacklist.
 *
 * @param {Request} req - Express request with URL as `id` parameter.
 * @param {Response} res - Express response object.
 */
exports.removeUrl = async (req, res) => {
    const url = req.params.id;
    if (!url) return res.status(400).json({ error: 'URL is required' });

    try {
        const response = await sendCommandString(`DELETE ${url}`);
        const statusLine = response.trim().split('\n')[0];
        if (statusLine.startsWith('204')) {
            res.status(204).send(statusLine);
        } else {
            res.status(404).send(statusLine);
        }
    } catch (err) {
        res.status(500).json({ error: 'Internal server error' });
    }
};

module.exports = {
    sendCommandString,
    checkUrl,
    addUrl: exports.addUrl,
    removeUrl: exports.removeUrl
};
