// blacklistService.js
const net = require('net');

/**
 * Send a plain command string over TCP to the blacklist server.
 * Returns a Promise that resolves to the string response.
 */
function sendCommandString(commandStr) {
    return new Promise((resolve, reject) => {
        const client = new net.Socket();
        let responseData = '';

        const host = 'blacklist_server'; // Docker service name
        const port = 12345;

        client.connect(port, host, () => {
            console.log('[DEBUG] Sending:', commandStr);
            client.write(commandStr + '\n');
        });

        client.on('data', (data) => {
            const chunk = data.toString();
            console.log('[DEBUG] Received chunk:', chunk);
            responseData += chunk;
        });

        client.on('end', () => {
            console.log('[DEBUG] Full response received:', responseData);
            resolve(responseData);
        });

        client.on('error', (err) => {
            console.error('[ERROR] Socket error:', err.message);
            reject(err);
        });

        client.setTimeout(3000, () => {
            console.warn('[WARN] Forcing socket close after timeout');
            client.end();
        });
    });
}

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
        console.error('Error adding URL:', err);
        res.status(500).json({ error: 'Internal server error' });
    }
};

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
        console.error('Error removing URL:', err);
        res.status(500).json({ error: 'Internal server error' });
    }
};

module.exports = {
    sendCommandString,
    checkUrl,
    addUrl: exports.addUrl,
    removeUrl: exports.removeUrl
};
