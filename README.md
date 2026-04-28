# 🚀 BranchCore - Integrated Management System (IMS)

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?style=for-the-badge&logo=vuedotjs)](https://vuejs.org/)
[![Ionic](https://img.shields.io/badge/Ionic-7-3880FF?style=for-the-badge&logo=ionic)](https://ionicframework.com/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019-red?style=for-the-badge&logo=microsoftsqlserver)](https://www.microsoft.com/sql-server)

**BranchCore** is a comprehensive enterprise solution designed to streamline operations across multiple retail branches. It integrates multi-channel management (Web Admin & Mobile App) to handle inventory, human resources, attendance tracking, and internal communications in real-time.

---

## 🌟 Key Features

### 1. Multi-Branch Inventory Ecosystem
- **Real-time Tracking:** Monitor stock levels across all branches with instant updates.
- **Approval Workflow:** Sophisticated multi-step approval process for stock transfers and requests.
- **Financial Integration:** Automated inventory valuation and financial reporting using Apache POI for Excel exports.

### 2. Smart Attendance & HR Management
- **GPS-Fence Verification:** Enforces attendance only within a specific radius of the store using high-accuracy geolocation.
- **Photo Identity Confirmation:** Captures employee photos during check-in/out to prevent "buddy punching".
- **Estimated Payroll:** Real-time salary estimation based on actual working hours and hourly rates.

### 3. Internal Communications (Internal News)
- **Centralized Broadcast:** Admin can publish internal announcements directly to the mobile app.
- **Auto-Expiry System:** Announcements are automatically cleaned up based on a set duration (1, 3, 7 days) using Spring Scheduling.

### 4. Real-time Infrastructure
- **WebSocket Notifications:** Instant alerts for stock requests, approval results, and system updates.
- **Security:** Robust authentication layer using Spring Security and JWT.

---

## 🛠 Tech Stack

### Backend
- **Core Framework:** Spring Boot 3.2.4 (Java 17)
- **Security:** Spring Security, JWT (Stateless authentication)
- **Persistence:** Spring Data JPA, Hibernate 6
- **Database:** Microsoft SQL Server
- **Caching:** Redis (Session & Data caching)
- **Communication:** WebSocket (STOMP/SockJS)
- **Tools:** Maven, Lombok, Apache POI

### Frontend (Web Admin)
- **Framework:** Vue.js 3 (Composition API)
- **UI Library:** Element Plus (Premium Meta-design system)
- **State Management:** Pinia
- **Build Tool:** Vite

### Mobile App (Hybrid)
- **Framework:** Ionic 7 + Vue 3
- **Native Bridge:** Capacitor (Camera, Geolocation)
- **Styling:** Custom Vanilla CSS for premium aesthetics

---

## 🏗 System Architecture

```mermaid
graph TD
    A[Mobile App - Ionic/Vue] -->|JWT Auth| B[API Gateway/Backend]
    C[Web Admin - Vue 3] -->|JWT Auth| B
    B --> D[Spring Boot Services]
    D --> E[(SQL Server)]
    D --> F[(Redis)]
    D --> G[WebSocket Broker]
    G -->|Real-time Alerts| A
    G -->|Real-time Alerts| C
```

---

## 🚀 Installation & Setup

### Prerequisites
- JDK 17+
- Node.js 18+
- SQL Server 2019+

### Backend Setup
1. Clone the repository
2. Update `application.properties` with your SQL Server credentials.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Web Admin Setup
1. Navigate to `/branch-management-fe`
2. Install dependencies: `npm install`
3. Start dev server: `npm run dev`

### Mobile App Setup
1. Navigate to `/chamcong_mobile`
2. Install dependencies: `npm install`
3. Run on Android/iOS: `npx cap open android`

---

## 📸 Demo Screenshots

| Dashboard (Web) | Attendance (Mobile) | Payroll (Mobile) |
| :---: | :---: | :---: |
| ![Web Dashboard](https://via.placeholder.com/300x180?text=Web+Dashboard) | ![Attendance](https://via.placeholder.com/150x300?text=Attendance+UI) | ![Payroll](https://via.placeholder.com/150x300?text=Payroll+UI) |

---

## 👨‍💻 Developer
**[Your Name]**
- Role: Fullstack Developer
- Project Scope: Graduation Project (DoAnTotNghiep)

---
*Developed with ❤️ by the Branch Management Team.*
