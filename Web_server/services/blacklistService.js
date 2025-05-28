// blacklistService.js
const net = require('net');

/**
 * Send a JSON command over TCP to the blacklist server.
 * Returns a Promise that resolves to the parsed JSON response.
 */
function sendCommand(command) {
    return new Promise((resolve, reject) => {
        const client = new net.Socket();
        let responseData = '';

        const host = 'blacklist_server'; // or 'localhost'
        const port = 12345;

        client.connect(port, host, () => {
            client.write(JSON.stringify(command) + '\n');
        });

        client.on('data', (data) => {
            responseData += data.toString();
            try {
                // try to parse full JSON
                const response = JSON.parse(responseData);
                client.destroy();
                resolve(response);
            } catch (_) {
                // wait for more data
            }
        });

        client.on('close', () => {
            // connection closed, attempt final parse
            try {
                const response = JSON.parse(responseData);
                resolve(response);
            } catch (err) {
                reject(new Error('Failed to parse server response'));
            }
        });

        client.on('error', (err) => {
            reject(err);
        });
    });
}

exports.checkUrl = async (req, res) => {
    const url = req.body.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    try {
        const response = await sendCommand({ action: 'GET', url });
        return res.json({ isBlacklisted: Boolean(response.isBlacklisted) });
    } catch (err) {
        console.error('Error checking URL:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
};

exports.addUrl = async (req, res) => {
    const url = req.body.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    try {
        const response = await sendCommand({ action: 'POST', url });
        return res.status(201).json(response);
    } catch (err) {
        console.error('Error adding URL:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
};

exports.removeUrl = async (req, res) => {
    const url = req.params.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    try {
        const response = await sendCommand({ action: 'DELETE', url });
        return res.status(200).json(response);
    } catch (err) {
        console.error('Error removing URL:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
};
