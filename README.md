# 🔐 DataPro - Secure Encrypted File Sharing System

## 📌 Overview

DataPro is a secure file management and sharing platform built using Spring Boot, MySQL, Supabase Storage, and JavaScript.

The system encrypts files before storing them in cloud storage, prevents duplicate uploads using SHA-256 hashing, and provides OTP-based secure sharing and access management.

---

## 🚀 Live Demo

https://datapro-bh55.onrender.com/login.html

---

## 💻 GitHub Repository

https://github.com/deveshagale12/DataPro

---

# ✨ Features

### User Features

* User Registration & Login
* Profile Management
* Secure File Upload
* AES-GCM Encryption
* SHA-256 Duplicate Detection
* Public & Private Files
* Community File Access
* Shared Files Dashboard
* OTP-Based File Access
* File Preview
* File Download
* Access Revocation

### Admin Features

* Dashboard Analytics
* User Management
* File Management
* Upload Statistics
* Role Distribution Charts
* Global Audit Logs
* User Deletion
* System Monitoring

---

# 🔒 Security Features

### AES-GCM Encryption

All uploaded files are encrypted before storage using AES-GCM.

Benefits:

* Confidentiality
* Integrity Validation
* Tamper Protection

### SHA-256 Hashing

Before encryption, file content is hashed using SHA-256.

Used for:

* Duplicate File Detection
* Content Verification

### OTP-Based Access

Files can only be accessed using a valid OTP.

Security Controls:

* OTP Expiry
* Email Verification
* Access Revocation

---

# 🏗️ System Architecture

User
↓
Spring Boot REST API
↓
AES-GCM Encryption
↓
SHA-256 Hash Generation
↓
Supabase Storage
↓
MySQL Metadata Storage
↓
OTP Verification
↓
Secure File Access

---

# 🛠️ Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* REST API
* Maven

## Database

* MySQL

## Cloud Storage

* Supabase Storage

## Frontend

* HTML5
* CSS3
* JavaScript
* Tailwind CSS
* Chart.js

## Deployment

* Render

## Email Services

* JavaMailSender
* SMTP
* Async Email Service

---

# 🧠 Algorithms Used

## AES-GCM Encryption

Encrypts files before cloud storage.

Complexity:

* Encryption: O(n)
* Decryption: O(n)

## SHA-256 Hashing

Generates unique file signatures.

Complexity:

* Hash Generation: O(n)

## OTP Generation

Secure random OTP generation.

Used For:

* File Sharing
* Access Verification

---

# 📂 Core Modules

## Authentication Module

* Login
* Registration
* OTP Verification
* Role Management

## File Module

* Upload
* Encryption
* Decryption
* Preview
* Download

## Sharing Module

* Share File
* Send OTP
* Verify OTP
* Revoke Access

## Community Module

* Public Files
* Access Requests

## Audit Module

Tracks:

* Uploads
* Downloads
* OTP Events
* Access Requests
* Access Revocations

---

# 📊 Admin Analytics

* Total Users
* Total Files
* Total Logs
* Upload Statistics
* Role Distribution
* Global Activity Monitoring

---

# 📧 Email Notifications

The system automatically sends:

* Upload Success Emails
* OTP Emails
* Access Request Emails
* Access Revocation Emails

---

# 🔮 Future Enhancements

* JWT Authentication
* Redis OTP Cache
* Kafka Event Notifications
* AWS S3 Storage
* Docker Deployment
* Kubernetes Support
* Multi-Factor Authentication
* Virus Scanning

---

# 👨‍💻 Developer

Devesh Agale

Java Developer | Full Stack Developer

GitHub:
https://github.com/deveshagale12

LinkedIn:
(Add your LinkedIn Profile URL here)

⭐ If you like this project, please star the repository.
