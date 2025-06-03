# Part C - Gmail-like Web Server
This project implements a multi-threaded web server using MVC architecture that provides Gmail-like functionality. The server integrates with a URL blacklist filter to validate URLs in emails.

---

## Description

The web server provides a RESTful API for a Gmail-like application with the following features:

1. **User Management**: Registration and authentication
2. **Email System**: Send, receive, manage, and draft emails with URL blacklist validation
3. **Labels**: Create and manage email labels/categories
4. **Blacklist Integration**: Integration with Exercise 2's URL blacklist filter
5. **Multi-threading**: Handle multiple concurrent clients

---

## Requirements

- Docker
- Docker Compose

## Getting Started

## Building and Running with Docker

Build the Docker image:
```bash
docker-compose build
```

Start both servers (Exercise 2 blacklist server and Exercise 3 web server):
```bash
docker-compose up
```

To stop all running containers:
```bash
docker-compose down
```

Example of a successful build + launch of the servers:

![build](screenshots/build.jpg)

---

## API Documentation

### Authentication Endpoints

#### User Registration

```
POST http://localhost:3000/api/users
Content-Type: application/json

{
  "fullName": "John Doe",
  "emailAddress": "john.doe@gmail.com",
  "birthDate": "1990-01-15",
  "gender": "male",
  "password": "password123"
}
```
#### User Details
```
GET http://localhost:3000/api/users/:id
Authorization: Bearer <user_id>
```

#### Login
```
POST http://localhost:3000/api/tokens
Content-Type: application/json

{
  "emailAddress": "john.doe@gmail.com",
  "password": "password123"
}
```

### Email Management

#### Get Inbox (Last 50 emails)
```
GET http://localhost:3000/api/mails
Authorization: Bearer <user_id>
```

#### Send Email or Save as Draft 
```
POST http://localhost:3000/api/mails
Authorization: Bearer <user_id>
Content-Type: application/json
{
  "to": "recipient@gmail.com",
  "subject": "Subject Line",
  "body": "Body content",
  "send": true       // true = send now, false = save as draft
}
```

- If `"send": true` → Validates URLs and sends email
- If `"send": false` or omitted → Saves email as draft

#### Get Specific Email
```
GET http://localhost:3000/api/mails/:id
Authorization: Bearer <user_id>
```

#### Update Email
```
PATCH http://localhost:3000/api/mails/:id
Authorization: Bearer <user_id>
Content-Type: application/json

{
  "subject": "Updated Subject",
  "body": "Updated body",
  "to": "newrecipient@gmail.com",
  "send": true    // If true, converts draft to sent email
}
```

- Only draft emails can be updated.
- If `"send": true`, the draft is deleted and the email is sent

#### Delete Email
```
DELETE http://localhost:3000/api/mails/:id
Authorization: Bearer <user_id>
```

- Emails are soft-deleted only for the requesting user.

#### Search Emails
```
GET http://localhost:3000/api/mails/search/:query
Authorization: Bearer <user_id>
```

### Label Management

#### Get All Labels
```
GET http://localhost:3000/api/labels
Authorization: Bearer <user_id>
```

#### Create Label
```
POST http://localhost:3000/api/labels
Authorization: Bearer <user_id>
Content-Type: application/json

{
  "name": "Work"
}
```

#### Get Specific Label
```
GET http://localhost:3000/api/labels/:id
Authorization: Bearer <user_id>
```

#### Update Label
```
PATCH http://localhost:3000/api/labels/:id
Authorization: Bearer <user_id>
Content-Type: application/json

{
  "name": "Updated Work"
}
```

#### Delete Label
```
DELETE http://localhost:3000/api/labels/:id
Authorization: Bearer <user_id>
```

### Blacklist Management

#### Add URL to Blacklist
```
POST http://localhost:3000/api/blacklist
Authorization: Bearer <user_id>
Content-Type: application/json

{
  "url": "http://malicious-site.com"
}
```

#### Remove URL from Blacklist
```
DELETE http://localhost:3000/api/blacklist/:id
Authorization: Bearer <user_id>
```

---

## HTTP Status Codes

The API returns appropriate HTTP status codes:

- **200 OK**: Successful GET requests
- **201 Created**: Successful POST requests (resource created)
- **204 No Content**: Successful DELETE/PATCH requests
- **400 Bad Request**: Malformed request or blacklisted URL detected
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## URL Blacklist Integration

When sending emails, all URLs in the email body are automatically checked against the blacklist server:

1. Email content is scanned for URLs
2. Each URL is validated against the blacklist server
3. If any URL is blacklisted, the email creation fails with `400 Bad Request`
4. Only emails with clean URLs are created and delivered
5. Drafts are not validated until they are sent

---

## Authentication

The server uses a simple token-based authentication system:

1. Users register with email Address, password, and profile details
2. Login returns a user ID token
3. Protected routes require the `Authorization: Bearer <user_id>` header
4. The user ID is validated for each protected request

### Public Endpoints (No Authentication Required):
- `POST /api/users` - User registration
- `POST /api/tokens` - User login

**Blacklist Management:**
- `POST /api/blacklist` - Add URL to blacklist
- `DELETE /api/blacklist/:id` - Remove URL from blacklist

### Protected Endpoints (Authentication Required):
All other endpoints require the `Authorization: Bearer <user_id>` header:

**User Management:**
- `GET /api/users/:id` - Get user details

**Email Management:**
- `GET /api/mails` - Get inbox
- `POST /api/mails` - Send email
- `GET /api/mails/:id` - Get specific email
- `PATCH /api/mails/:id` - Update email
- `DELETE /api/mails/:id` - Delete email
- `GET /api/mails/search/:query` - Search emails

**Label Management:**
- `GET /api/labels` - Get all labels
- `POST /api/labels` - Create label
- `GET /api/labels/:id` - Get specific label
- `PATCH /api/labels/:id` - Update label
- `DELETE /api/labels/:id` - Delete label

--- 

## Running Example

### User Registration and Login
```bash
# Register a new user
curl -X POST http://localhost:3000/api/users \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice Smith","emailAddress":"alice@gmail.com","birthDate":"1990-05-10","gender":"female","password":"password123"}'

# Login
curl -X POST http://localhost:3000/api/tokens \
  -H "Content-Type: application/json" \
  -d '{"emailAddress":"alice@gmail.com","password":"password123"}'
```

### Send and Retrieve Emails
```bash
# Send email (replace USER_ID with actual ID from login)
curl -X POST http://localhost:3000/api/mails \
  -H "Authorization: Bearer USER_ID" \
  -H "Content-Type: application/json" \
  -d '{"to":"bob@gmail.com","subject":"Hello","body":"Hi Bob! Check out https://example.com", "send":true}'

# Get inbox
curl -X GET http://localhost:3000/api/mails \
  -H "Authorization: Bearer USER_ID"
```

Example server output:

![Server Running](screenshots/server_running.png)

---

## Data Persistence

This server uses in-memory storage. All data (users, emails, labels, drafts) is lost when the server restarts.

The blacklist server maintains its own persistence for URL validation.
