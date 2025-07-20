# 📝 Registration & Login

This page explains how users register and login to the system, both on the Web client and Android client.

---

## 🌐 Web Client

### 1. Registration

- On the login page, click the `Create Account` button.
- Fill out all required fields (you can also add aprofile picture).
- Click the `Register` button.
- After successful registration, you will be redirected to the login page.

<p align="center">
    <img src="images/web_reg_1.png" width="80%" />
    <p align="center">
    <img src="images/web_reg_2.png" width="80%" />
</p>

### 2. Login

- Enter your email and password.
- Click the `Log in` button.
- If login is successful, you will be redirected to the inbox page.
<p align="center">
    <img src="images/login_web.png" width="80%" />
    <p align="center">
    <img src="images/first_inbox_web.png" width="80%" />
</p>
---

## 📱 Android Client

### 1. Registration

- On the login screen, tap the `Create Account` button.
- Fill out all required fields (profile picture is optional).
- Tap the `Register` button.
- After successful registration, you will be redirected to the login screen.

<p align="center">
    <img src="images/app_register.jpg" width="30%">
</p>

### 2. Login

- Enter your email and password.
- Tap the `Log in` button.
- If login is successful, you will be redirected to the inbox screen.

<p align="center">
    <img src="images/app_login.jpg" width="30%" hspace="10">
    <img src="images/app_first_inbox.jpg" width="30%" hspace="10">
</p> 

---

## ❗ Error Handling & Validation

### Registration Errors (Web)

- All required fields are validated before submission.
- Passwords must include at least 8 characters, one uppercase letter, and one number.
- Users are informed if an email is already registered or if any fields are invalid.

<p align="center">
    <img src="images/reg_error.jpg" width="30%" />
</p>

### Login Errors (Web)

- If login credentials are incorrect, an error message is displayed clearly to the user.

<p align="center">
    <img src="images/login_error.jpg" width="40%" />
</p>

> All validation is handled both on the client-side and server-side to ensure data integrity and user experience.

---

## ⚙️ Technical Details (Optional)

- Registration sends a POST request to `/api/users` with user details (including multipart/form-data for profile image).
- Login sends a POST request to `/api/tokens` with email and password.
- On success, login returns a JWT token and user info, saved in local storage (Web) or SharedPreferences (Android).
- Basic input validation is performed on the client side.

---
