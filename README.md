# DukanKonnect - Compose Multiplatform Service Booking App 

DukanKonnect is a comprehensive service booking application designed to seamlessly connect local vendors and service providers with customers. Whether you need a home repair, salon appointment, or freelance services, DukanKonnect offers an intuitive and responsive platform to discover, book, and manage services locally.

Built with **Compose Multiplatform (CMP)**, this project serves as a technical showcase of cross-platform mobile development for Android and iOS sharing a unified Kotlin codebase and 100% shared UI.

The application's backend is powered by a **Spring Boot** server, while leveraging **Supabase** for database management and cloud storage.

---

##  App Previews

| Android | iOS |
| :---: | :---: |
| <video src="https://github.com/user-attachments/assets/c9a35b3d-13f5-4a5f-a9bb-7fce793d0f82" controls height="200"></video> | <video src="https://github.com/user-attachments/assets/134302f2-64a3-4ed6-90ab-a1baa1a01c2a" controls height="200"></video> |

---

##  Key Features

* **Service Discovery & Booking:** Browse local service providers, view details, and book appointments with an interactive UI for precise time and location selection.
* **Comprehensive Booking Management:** Dedicated dashboard for users to track, edit, or cancel active bookings, clearly categorized into **Pending**, **Completed**, and **Cancelled** states.
* **Integrated Payments:** End-to-end payment flow simulation native to each platform.
* **Cross-Platform Notifications:** Real-time push notification handling implemented for both Android and iOS.
* **Platform-Aware Store Rating:** Smart navigation to the Google Play Store (Android) or Apple App Store (iOS) when users click "Rate Us" from the profile screen.

---

##  Technical Architecture & Implementation

This repository goes beyond UI sharing to demonstrate production-ready architectural patterns and integrations in Compose Multiplatform.

### Architecture Choices
The application follows a clean, modular architecture, separating the UI layer from business logic and data sources. Utilizing Kotlin Coroutines and Flows, the app ensures reactive and asynchronous data handling across the shared codebase, keeping the UI perfectly in sync with the underlying state.

### State Management & Caching
To ensure a snappy user experience and minimize unnecessary network calls, strategic caching is implemented:
- **Home & Booking Screens:** Frequently accessed data (like available services and user booking history) are cached locally. The UI instantly reflects the cached state, masking loading times while silently fetching the latest data in the background to ensure freshness.

### Native Payments Integration
Handling payments requires platform-specific precision. DukanKonnect leverages Kotlin Multiplatform's `expect`/`actual` paradigm to interface directly with native SDKs:
- **Android:** Integrates directly with native Android payment APIs.
- **iOS:** Natively interacts with iOS payment solutions, ensuring compliance and platform-standard user experiences.

### Push Notifications & iOS Dependency Management
Real-time updates are critical for managing service bookings.
- **Android:** Handled natively via Firebase Cloud Messaging (FCM).
- **iOS:** Push notifications are integrated into the iOS lifecycle. To manage complex native iOS dependencies in the shared Kotlin environment, **CocoaPods** integration is utilized extensively, bridging the gap between native Swift/Objective-C libraries and the shared KMP code.

### Mapping with MapLibre
For interactive location selection and viewing service providers on a map, **MapLibre** is integrated into the shared UI.
- **Why MapLibre?** MapLibre was chosen over proprietary SDKs (like Google Maps) for its excellent cross-platform support, performant vector tile rendering, and open-source nature. It bypasses the heavy licensing and integration complexities often associated with embedding platform-specific map views in a Compose Multiplatform context, providing a unified and customizable map experience on both Android and iOS.

### Robust Error Handling
Network flakiness and unexpected API responses are managed gracefully across the app. The architecture utilizes sealed classes (`Result` wrappers) to strictly model `Success`, `Loading`, and various `Error` states. This guarantees that network timeouts, authentication failures, or empty states are caught and presented to the user with helpful, actionable UI prompts, completely avoiding generic crashes.

---

##  Links

- **[Spring Boot Backend Repository](https://github.com/Dukan-Konnect/dukan-backend)**
