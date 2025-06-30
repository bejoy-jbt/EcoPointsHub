# ♻️ EcoPoints Hub – Recycle, Earn, Sustain

**EcoPoints Hub** is an Android-based mobile application that promotes sustainable waste management by connecting users and waste collectors on a unified platform. Users can post recyclable items, and collectors can accept and manage pickup requests. EcoPoints are awarded based on the type and quantity of waste submitted and can be redeemed for rewards.

## 🚀 Features

- 🔐 **Authentication**
  - Secure login & registration using Firebase Authentication
  - Role-based access: User or Collector

- ♻️ **Waste Listing**
  - Post waste by type, quantity, and location
  - Collectors browse and accept pickup requests

- 🧾 **EcoPoints System**
  - Earn points based on waste category and weight
  - Redeem points for rewards or in-app marketplace

- 🧑‍🤝‍🧑 **Marketplace**
  - Trade recyclable items directly with other users
  - Transparent listing with condition, pricing, and location

- 💬 **Chat Module**
  - Real-time messaging between users and collectors

- 📊 **User Dashboard**
  - Profile management
  - View EcoPoints, history, and activity status

## 🛠️ Tech Stack

| Layer         | Technology                     |
|---------------|-------------------------------|
| Frontend      | Android (Java), XML           |
| Backend       | Firebase Firestore (NoSQL DB) |
| Authentication| Firebase Authentication       |
| Cloud Storage | Firebase Cloud Storage        |
| Notifications | Firebase Cloud Messaging      |
| Analytics     | Firebase Analytics            |
| Security      | SHA-256, Firebase Security Rules |

## 🧩 Modules Overview

### 🔐 Authentication
- Email/password sign-in
- Role-based dashboard redirection

### ♻️ Waste Listing
- Post, view, update, and delete waste listings
- Collectors can accept and manage pickups

### 💬 Chat System
- Direct and secure chat between users and collectors

### 🧑 Account Module
- Profile update, EcoPoints tracker, and settings

## 🧪 Testing

The app has undergone comprehensive testing to ensure:
- ✅ Unit tests on Firebase integration, logic, and forms
- 🔗 Integration tests across listing and pickup workflows
- 📱 Compatibility across Android API 23+ devices
- 🔒 Security and access control validation

## 📦 Installation & Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/bejoy-jbt/EcoPointsHub.git
## 📄 License

This project is licensed under the [MIT License](LICENSE).
