# 📱 Branch Attendance Mobile App

[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4fc08d)](https://vuejs.org/)
[![Ionic](https://img.shields.io/badge/Ionic-Framework-3880ff)](https://ionicframework.com/)
[![Capacitor](https://img.shields.io/badge/Capacitor-Native-119eff)](https://capacitorjs.com/)

A modern, cross-platform mobile application for employees to manage their work life. Built with **Ionic Framework** and **Vue 3**, it provides a seamless experience for attendance tracking and shift management.

## ✨ Features

- **Quick Check-in/Out**: One-tap attendance with identity verification.
- **Biometric/Identity Verification**: Integrates camera to capture verification photos.
- **GPS Location Tracking**: Automatic background location validation.
- **Shift Schedule**: View upcoming and past work schedules.
- **Leave Request**: Submit and track time-off requests directly from the phone.
- **History Logs**: Detailed history of attendance records.

## 🛠️ Tech Stack

- **Framework**: Ionic + Vue 3
- **State Management**: Vue Composition API (Refs/Reactive)
- **Networking**: Axios with centralized API client
- **Native Bridge**: Capacitor (Camera & Geolocation APIs)
- **Styling**: Scoped CSS with Ionic Design System

## 🚀 Key Implementation Details

- **Native Device Access**: Uses `@capacitor/geolocation` for high-accuracy GPS and `MediaDevices` for camera stream.
- **Clean UI/UX**: Follows mobile design patterns with intuitive navigation and feedback (Toasts/Loaders).
- **Multipart Data Handling**: Correctly handles binary photo uploads along with metadata.
- **Responsive Design**: Optimized for various screen sizes and orientations.

## 📦 Installation

1. Ensure you have Node.js installed.
2. Clone the repository.
3. Install dependencies:
   ```bash
   npm install
   ```
4. Run the development server:
   ```bash
   ionic serve
   ```
5. Build for Android/iOS:
   ```bash
   ionic build
   npx cap add android
   npx cap sync
   ```

---
*Part of the Branch Management System Ecosystem.*
