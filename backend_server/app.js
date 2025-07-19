const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');

const app = express();

// Import the authentication middleware
const authenticateToken = require('./middlewares/authMiddleware'); // Correct path: should be middlewares, not middleware

const corsOptions = {
  origin: 'http://localhost:8080',
  methods: ['GET', 'POST', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
};

// Connect to MongoDB
const DB_URI = process.env.MONGO_URI || 'mongodb://mongodb:27017/smail-mongo';

mongoose.connect(DB_URI)
.then(() => console.log('MongoDB connected successfully'))
.catch(err => console.error('MongoDB connection error:', err));

app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use(cors(corsOptions));

// Middleware for decoding JSON in requests
app.use(express.json());

// Imports the files that define the paths for each part of the system
const usersRoutes = require('./routes/userRoutes'); // Assuming this is the correct single import for user routes
const mailsRoutes = require('./routes/mailRoutes');
const labelsRoutes = require('./routes/labelRoutes');
const blacklistRoutes = require('./routes/blacklistRoutes');
const tokensRoutes = require('./routes/tokenRoutes');


// Connects each group of routes to the main API

// Routes that do NOT require authentication (e.g., user registration, login via tokens)
app.use('/api/users', usersRoutes);
app.use('/api/tokens', tokensRoutes);

// Apply authentication middleware to routes that require it
// This is the crucial part that was missing or incorrect
app.use('/api/mails', authenticateToken, mailsRoutes);
app.use('/api/labels', authenticateToken, labelsRoutes);
app.use('/api/blacklist', authenticateToken, blacklistRoutes);


// Handling 404
app.use((req, res) => {
  res.status(404).json({ error: 'Not Found' });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});

module.exports = app;
