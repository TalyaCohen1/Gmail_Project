const express = require('express');
const cors = require('cors');

const app = express();
const path = require('path');

const corsOptions = {
  origin: 'http://localhost:8080',
  methods: ['GET', 'POST', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
};

app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use(cors(corsOptions));

// Middleware for decoding JSON in requests
app.use(express.json());

// Imports the files that define the paths for each part of the systemconst usersRoutes = require('./routes/usersRoutes');
const mailsRoutes = require('./routes/mailRoutes');
const labelsRoutes = require('./routes/labelRoutes');
const blacklistRoutes = require('./routes/blacklistRoutes');
const tokensRoutes = require('./routes/tokenRoutes');
const usersRoutes = require('./routes/userRoutes');

// Connects each group of routes to the main API
app.use('/api/users', usersRoutes);
app.use('/api/mails', mailsRoutes);
app.use('/api/labels', labelsRoutes);
app.use('/api/blacklist', blacklistRoutes);
app.use('/api/tokens', tokensRoutes);

// Handling 404
app.use((req, res) => {
  res.status(404).json({ error: 'Not Found' });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0'); 

module.exports = app;