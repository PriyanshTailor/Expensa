# Smart Expense Tracker (Android + Firebase)

This project is a professional-grade Expense Tracker built with a **Native Android** frontend and a **Spring Boot** backend integrated with **Firebase Firestore**.

## 📁 Project Structure
- **`android_java/`**: The main Android Studio project.
- **`backend_java/`**: The Spring Boot API that handles data and security.

## 🚀 How to Run

### 1. Setup Firebase
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Enable **Firestore Database** in "Test Mode".
3.  Go to **Project Settings > Service Accounts** and click **Generate new private key**.
4.  Save the JSON file as `serviceAccountKey.json` and put it in `backend_java/src/main/resources/`.

### 2. Run the Backend
1.  Open Android Studio.
2.  Go to `File > Open` and select the **`backend_java`** folder.
3.  Wait for the Maven sync to finish (click the "Reload" icon in the Maven tab if needed).
4.  Run `BackendApplication.java`.

### 3. Run the Android App
1.  In a **new Android Studio window**, go to `File > Open`.
2.  Select the **`android_java`** folder.
3.  Wait for Gradle sync.
4.  Connect your phone or start an emulator and click **Run**.

## ✨ Key Features
- **SMS Transaction Tracking**: Automatically detects bank/UPI messages and logs expenses.
- **Invest Module**: Quick links to Mutual Funds, FD, and Indian Post schemes.
- **Learn Module**: Educational financial content and YouTube tutorials.
- **Cloud Sync**: Secure data storage via Firebase Firestore.
