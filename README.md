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



## 📸 Screenshots

- **My Profile** — User profile overview with editable personal details and account actions.  
  <img width="350" height="770" alt="Screenshot_profile_screen" src="https://github.com/user-attachments/assets/92657909-5cd9-4a22-9cf4-9cb873c2261e" />

- **Registration (Interests & Goal)** — Onboarding form with sport preferences, username, goal, and birthday picker.  
  <img width="350" height="770" alt="Screenshot_registration_screen" src="https://github.com/user-attachments/assets/e9d0fb49-4539-4c39-b528-30d81272d60e" />

- **Navigation Drawer (Scaffold)** — Main drawer navigation to key modules (profile, friends, workouts, chatbot, messages).  
  <img width="350" height="770" alt="Screenshot_scaffold_screen" src="https://github.com/user-attachments/assets/d5166c42-2c33-4f5c-9a94-cc3cba10f1ed" />

- **Find a Friend** — Search users by username and send a friend request.  
  <img width="350" height="770" alt="Screenshot_search_screen" src="https://github.com/user-attachments/assets/adca0166-2b42-4cee-a043-ac30681e04fc" />

- **Workouts: Sort & Filter** — Bottom sheet for sorting and filtering workouts by type, status, and order.  
  <img width="350" height="770" alt="Screenshot_sort_and_filter" src="https://github.com/user-attachments/assets/bb41a26f-cc93-4b65-b94a-d5db0d2f0f9d" />

- **Workouts List** — Workouts overview with status, duration, summary stats, and quick actions.  
  <img width="350" height="770" alt="Screenshot_workouts_list" src="https://github.com/user-attachments/assets/06d04278-0457-41df-ae6a-b8c51eedfbdc" />

- **Edit Workout** — Edit workout status, date/time, and manage exercises/sets.  
 <img width="350" height="770" alt="Screenshot_workouts_list" src="https://github.com/user-attachments/assets/739c4160-40bf-469b-a068-d803bc235fb8" />

- **Sign Up** — Email/password account creation screen.  
  <img width="350" height="770" alt="signUpScreen" src="https://github.com/user-attachments/assets/054e5587-f875-4021-bc2b-585e71f68eb5" />

- **Sign In** — Email-based sign-in entry screen.  
  <img width="350" height="770" alt="singIn_screen" src="https://github.com/user-attachments/assets/f48e4ddd-f21f-4a7f-a693-57b137651611" />

- **Add Workout** — Create a workout (type, status, date/time) and add exercises/sets.  
  <img width="350" height="770" alt="Screenshot_add_workout" src="https://github.com/user-attachments/assets/785051bc-54aa-4c7e-9b40-ceb0a39b0859" />

- **Workout Reminder** — Optional pre-workout notification timing selection.  
  <img width="350" height="770" alt="Screenshot_workout_reminder" src="https://github.com/user-attachments/assets/e412f16e-5489-4c90-9ea1-903d16fe8cdd" />

- **AI ChatBot** — Built-in AI assistant chat for fitness-related guidance (prototype).  
  <img width="350" height="770" alt="Screenshot_ai_chatbot" src="https://github.com/user-attachments/assets/29723afe-bef9-4ea1-b1f6-62ba346e0fb0" />

- **Chat Screen** — 1:1 conversation view with message history and media preview.  
  <img width="350" height="770" alt="Screenshot_chat_screen" src="https://github.com/user-attachments/assets/2ef62424-1c73-4ff2-92bb-955a0b769e03" />

- **Messages** — Conversations list (chat entries / last message preview).  
  <img width="350" height="770" alt="Screenshot_message_screen" src="https://github.com/user-attachments/assets/27849f7d-9cf8-4669-9843-e0c310ef968d" />

- **Friend Invitations** — Incoming friend requests with accept/decline actions.  
  <img width="350" height="770" alt="Screenshot_friend_invitations" src="https://github.com/user-attachments/assets/8c046250-7d5f-486e-a50b-5c7a387b71fc" />

- **Sign In (Credential Manager)** — Account chooser for fast sign-in (saved credentials / Google).  
  <img width="350" height="770" alt="Screenshot_credential_manager" src="https://github.com/user-attachments/assets/d8c844df-350f-4cd6-9556-ba03c8dfaf1b" />

## ⚠️ Project Status / Disclaimer

This application is **not production-ready** — it is a **prototype / learning project**.  
You may encounter **bugs** and unfinished flows.

Also: the codebase was created **some time ago**, so some parts reflect older coding habits and patterns. If I were implementing the same features today, I would refactor several areas and structure some modules differently.
