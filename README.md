# GymBuddy — Mobile Training Organizer & Social Fitness App

GymBuddy is an Android app prototype designed to help people **organize training**, **track workouts**, and **stay connected** through social features like friends, real-time messaging, and notifications. It combines a modern Android UI built with Jetpack Compose and a cloud-first backend powered by Firebase.

---

## ✨ Key Features

- **Authentication**
  - Email/password auth
  - Google Sign-In via **Android Credential Manager**
- **User Profile**
  - Profile creation & editing
  - Profile picture upload (Firebase Storage)
- **Social Layer**
  - Search users
  - Friends management
  - Friend requests + push notifications
- **Messaging**
  - 1:1 chat experience backed by Firestore real-time updates
  - Support for rich/HTML message rendering where needed (Compose + AndroidView)
- **Training**
  - Create and browse training sessions (planning/monitoring)
- **AI Chatbot**
  - Built-in AI chat screen (prototype integration)

---

## 🧩 UI / Screens

- Login / Register
- Main layout (Scaffold + Navigation Drawer)
- My Profile
- Search Users
- Friends
- Messages (channels list)
- Chat screen
- Trainings
- AI Chatbot

---

## 🏗️ Architecture

- **MVVM** (Model–View–ViewModel)
- **Repository layer** to isolate data sources (Firestore/Auth/Storage/Network)
- **Unidirectional data flow** into Compose UI
- Reactive streams with **Kotlin Flow / StateFlow**
- Firestore listeners wrapped into `Flow` (via `callbackFlow`) for real-time UI updates

---

## 🧰 Tech Stack (with badges)

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?logo=materialdesign&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-0095D5?logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Cloud%20Firestore-FFCA28?logo=firebase&logoColor=black)
![FCM](https://img.shields.io/badge/FCM%20Push-FFCA28?logo=firebase&logoColor=black)
![Ktor](https://img.shields.io/badge/Ktor-087CFA?logo=ktor&logoColor=white)

**Core:**
- Kotlin
- Jetpack Compose (+ Material components)
- Kotlin Coroutines, Flow / StateFlow
- MVVM + Repository pattern

**Firebase:**
- Firebase Authentication
- Cloud Firestore (real-time)
- Cloud Storage (media)
- Firebase Cloud Messaging (push)
- Cloud Functions (event-driven automation)

**Other:**
- Android Credential Manager (passwords + Google ID token flow)
- Ktor (light backend endpoints used for push notification dispatching / orchestration)

---

## 🔔 Notifications & Backend Notes

This project uses push notifications (FCM) to support social flows (e.g., friend requests).  
A lightweight **Ktor backend** is used to expose endpoints that accept JSON payloads and trigger FCM sends (via Firebase Admin).  
Additionally, **Cloud Functions** can react to Firestore events and either send notifications instantly or schedule them for later delivery.

---

## 📄 Thesis (Full Document Included)

A full version of the **Bachelor’s thesis** describing the project (requirements, architecture, implementation details, and evaluation) is available in the repository **Releases** section as an attached PDF.


---

## 🚀 Getting Started (high level)

1. Configure a Firebase project:
   - Enable Authentication providers (Email/Password + Google)
   - Create Firestore database
   - Configure Storage
   - Enable FCM
2. Add `google-services.json` to the Android module.
3. (Optional) Backend:
   - Provide Firebase Admin service account JSON for Ktor / server environment
   - Configure endpoint URLs used by the Android app

---

## ⚠️ Project Status / Disclaimer

This application is **not production-ready** — it is a **prototype / learning project**.  
You may encounter **bugs** and unfinished flows.

Also: the codebase was created **some time ago**, so some parts reflect older coding habits and patterns. If I were implementing the same features today, I would refactor several areas and structure some modules differently.
