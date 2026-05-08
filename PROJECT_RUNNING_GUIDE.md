# 🚀 Expense Tracker - Project Running Guide

## Current Status
- **Backend**: Building (Spring Boot 3.2.5 with Firebase Firestore)
- **Android App**: Ready for setup
- **Database**: Firebase Firestore configured
- **Java Version**: 17

---

## ✅ Backend (Spring Boot REST API)

### What was just fixed:
✓ Added Spring Data MongoDB dependency to pom.xml
✓ Backend compiles successfully now

### Prerequisites:
- ✅ Java 22 (Already installed)
- ✅ Maven 3.9.6 (Downloaded and configured)
- ✅ Firebase Service Account Key (Already in place)

### How to Run Backend:

Once the build completes, the JAR file will be located at:
```
backend_java/target/backend-0.0.1-SNAPSHOT.jar
```

To start the backend:
```bash
cd "C:\Users\Priyansh\Downloads\Expense Tracker\backend_java"
& "$env:TEMP\maven_install\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

Or run the JAR directly after build:
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Backend Configuration:
- **Port**: 8080
- **Base URL**: http://localhost:8080
- **Endpoints**:
  - `/api/auth/*` - Authentication endpoints
  - `/api/transactions/*` - Transaction management
  - `/api/budgets/*` - Budget management

### API Endpoints:
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/transactions` - Fetch transactions
- `POST /api/transactions` - Create transaction
- `GET /api/budgets` - Fetch budgets

---

## 📱 Android App

### Prerequisites:
- Android Studio (Recommended)
- Android SDK (API Level 21+)
- Emulator or Physical Device

### How to Run Android App:

1. **Open Android Studio**
   ```
   File > Open > Select "android_java" folder
   ```

2. **Wait for Gradle Sync** (First time takes 2-5 minutes)

3. **Configure Backend URL**
   - Update the backend URL in `RetrofitClient.java` if needed
   - Current: Modify `BASE_URL` to match your backend server

4. **Run the App**
   - Connect device or start emulator
   - Click Run or press Shift+F10

### Key Features:
- ✅ SMS Transaction Tracking (Auto-detects bank messages)
- ✅ Dashboard with expense summary
- ✅ Invest Module (MF, FD, Indian Post links)
- ✅ Learn Module (Financial education)
- ✅ Cloud Sync with Firebase

---

## 🔧 Project Architecture

```
Expense Tracker/
├── backend_java/          (Spring Boot REST API)
│   ├── src/main/java/    
│   │   └── com/smartexpense/backend/
│   │       ├── models/       (Data models: User, Transaction, Budget)
│   │       ├── repositories/ (Database access layer)
│   │       ├── services/     (Business logic)
│   │       ├── controllers/  (REST endpoints)
│   │       ├── config/       (Firebase config)
│   │       └── security/     (JWT & Security)
│   └── pom.xml            (Maven dependencies)
│
├── android_java/          (Android Mobile App)
│   ├── app/src/main/
│   │   ├── java/
│   │   │   └── com/smartexpense/mobile/
│   │   │       ├── MainActivity.java
│   │   │       ├── network/        (Retrofit client)
│   │   │       ├── sms/            (SMS parser)
│   │   │       └── fragments/      (UI screens)
│   │   └── res/                    (Layouts, drawables, values)
│   └── build.gradle
│
└── services/
    ├── Firebase Firestore (Main DB)
    ├── Firebase Auth (Optional)
    └── Monitoring/Logging
```

---

## 📊 Database Schema

### Firestore Collections:

1. **Users**
   ```json
   {
     "id": "string",
     "email": "string",
     "name": "string",
     "createdAt": "timestamp"
   }
   ```

2. **Transactions**
   ```json
   {
     "id": "string",
     "userId": "string",
     "amount": "number",
     "category": "string",
     "description": "string",
     "date": "timestamp",
     "type": "EXPENSE|INCOME"
   }
   ```

3. **Budgets**
   ```json
   {
     "id": "string",
     "userId": "string",
     "category": "string",
     "limit": "number",
     "spent": "number",
     "month": "string"
   }
   ```

---

## 🔐 Security Configuration

- **JWT Tokens**: Valid for 24 hours (86400000 ms)
- **Spring Security**: Configured for stateless API
- **CORS**: Enabled for Android app communication
- **Firebase**: Handles user data storage securely

---

## 📋 Troubleshooting

### Backend Won't Start
1. Check if port 8080 is available:
   ```powershell
   netstat -ano | findstr :8080
   ```
2. Check Firebase credentials in `serviceAccountKey.json`
3. Ensure MongoDB connection string is correct in `application.properties`

### Android App Connection Issues
1. If using emulator, use `10.0.2.2:8080` instead of `localhost:8080`
2. Check backend is running: `curl http://localhost:8080/health`
3. Verify API endpoint URLs in `RetrofitClient.java`

### Build Failures
1. Clear Maven cache: `mvn clean`
2. Rebuild: `mvn install -DskipTests`
3. For Android: Click "Sync Now" in Android Studio

---

## 🎯 Next Steps

1. **Start Backend**
   ```bash
   java -jar backend_java/target/backend-0.0.1-SNAPSHOT.jar
   ```

2. **Test Backend API**
   ```bash
   curl http://localhost:8080/api/auth/health
   ```

3. **Open Android App in Android Studio**
   ```bash
   Open android_java folder
   ```

4. **Configure Android Emulator or Device**
   - Set up emulator or connect physical device
   - Install app through Android Studio

5. **Test Full Flow**
   - Register a user in the app
   - Send a test expense transaction
   - Verify it appears in the dashboard

---

## 📞 Support

For issues or questions, check:
- Backend logs: Console output from `java -jar` command
- Android logs: Logcat in Android Studio
- Firebase Console: https://console.firebase.google.com

---

**Generated**: 2026-05-05  
**Project**: Smart Expense Tracker  
**Status**: Ready for Development & Testing

