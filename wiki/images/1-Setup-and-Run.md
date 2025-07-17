# 📦 Full Environment Setup & Run Guide

This guide explains how to set up and run the entire project using Docker and `docker-compose`, including instructions for configuring the Android application.

---

## 📁 Prerequisites

Before running the system, make sure the following tools are installed:

- [✔️ Docker](https://docs.docker.com/get-docker/)
- [✔️ Docker Compose](https://docs.docker.com/compose/install/)
- [✔️ Git](https://git-scm.com/)
- [✔️ Android Studio](https://developer.android.com/studio) (if running the Android app)


---

## 🚀 Step 1: Clone the Project

Open a terminal and run the following:

```bash
git clone https://github.com/TalyaCohen1/Gmail_Project.git
cd your-repo-name

---

## Step 2: Start the System with Docker Compose

Run the following command from the project root:

```bash
Copy code
docker-compose up --build
This will build and start all services:

Node.js backend (port 3000)

MongoDB database

React frontend (port 8080)

To stop the services:
docker-compose down


## Step 3: Android Configuration
If you're working with the Android app, make sure to configure the IP address for backend access.

🔧 1. Update local.properties
Navigate to android_app/local.properties.

If you're using an emulator, use this special IP address:

properties
Copy code
backend_ip=10.0.2.2
If you're using a real device, replace 10.0.2.2 with your computer's local IP address (e.g., 192.168.1.102).

🖼️ Recommended screenshot: open local.properties in Android Studio and highlight the backend_ip line.

🔐 2. Update network_security_config.xml
Go to:

bash
Copy code
android_app/app/src/main/res/xml/network_security_config.xml
Make sure this file allows cleartext traffic (for development purposes):

xml
Copy code
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
💡 If you're only testing with an emulator and using 10.0.2.2, this file is required to allow HTTP requests (instead of HTTPS).

🖼️ Recommended screenshot: show the full contents of network_security_config.xml.

📲 Step 4: Run the Android App
Open android_app folder in Android Studio.

Wait for Gradle sync to complete.

Make sure an emulator or device is running.

Press ▶️ to build and run the app.

🖼️ Recommended screenshot: Android Studio with emulator running, and app opened to login/registration page.