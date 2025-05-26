// blacklistService.js
const net = require('net');

// Helper to send a command to the blacklist server
function sendCommand(command, callback) {
    const client = new net.Socket();
    let responseData = '';

    const host = 'localhost';
    const port = 3001;

    client.connect(port, host, () => {
        client.write(JSON.stringify(command) + '\n');
    });

    client.on('data', (data) => {
        responseData += data.toString();

        try {
            const response = JSON.parse(responseData);
            client.destroy();
            callback(null, response);
        } catch (err) {
            // wait for more data
        }
    });

    client.on('close', () => {
        try {
            const response = JSON.parse(responseData);
            callback(null, response);
        } catch (err) {
            callback(new Error('Failed to parse server response'));
        }
    });

    client.on('error', (err) => {
        callback(err);
    });
}

exports.checkUrl = (req, res) => {
    const url = req.body.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    sendCommand({ action: 'GET', url }, (err, response) => {
        if (err) {
            console.error('Error checking URL:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

       return res.json({ isBlacklisted: response.isBlacklisted });
    });
};

exports.addUrl = (req, res) => {
    const url = req.body.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    sendCommand({ action: 'POST', url }, (err, response) => {
        if (err) {
            console.error('Error adding URL:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        return res.status(201).json(response);
    });
};

exports.removeUrl = (req, res) => {
    const url = req.params.url;
    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    sendCommand({ action: 'DELETE', url }, (err, response) => {
        if (err) {
            console.error('Error removing URL:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        return res.status(200).json(response);
    });
};
