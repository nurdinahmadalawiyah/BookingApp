# Booking App

A modern Android reservation application built with **Jetpack Compose** and following **Clean Architecture + MVVM** principles. This app is designed for high performance, maintainability, and a seamless user experience.

## Key Features
* **Modern UI/UX**: Built entirely with Jetpack Compose and Material 3 design system.
* **Offline-First**: Robust local storage using Room Database for uninterrupted usage.
* **Efficient Networking**: Integrated with Retrofit and OkHttp for reliable API communication.
* **Background Processing**: Reliable background tasks handled by WorkManager.
* **Analytics & Stability**: Real-time crash reporting and usage analytics via Firebase.

---

## Tech Stack & Libraries

### UI Layer
* **Jetpack Compose**: Native UI toolkit for modern Android development.
* **Material 3**: Google's latest design language.
* **Core Splashscreen**: Smooth app entry transition.
* **Extended Icons**: Full library of Material icons for enhanced UI.

### Dependency Injection & Architecture
* **Hilt (Dagger)**: Standard library for dependency injection.
* **Clean Architecture**: Separation of concerns between Data, Domain, and Presentation layers.
* **MVVM**: Model-View-ViewModel pattern for the UI layer.

### Data & Networking
* **Retrofit & Gson**: Type-safe HTTP client and JSON serialization.
* **Room Database**: SQLite abstraction for local data persistence.
* **DataStore Preferences**: Modern data storage solution for key-value pairs.

### Background & Reliability
* **WorkManager**: Task scheduling (Hilt-integrated).
* **Firebase (BOM)**: Comprehensive suite for Crashlytics and Analytics.

---

## API Reference

This application consumes the **RESTful-booker** API, a ready-to-use platform for testing booking-related logic.

* **Base URL**: `https://restful-booker.herokuapp.com`
* **Documentation**: [Restful-booker API Docs](https://restful-booker.herokuapp.com/apidoc/index.html)
* **Features Used**: Auth (Create Token), Booking (Get, Create, Update, Delete).

---

## Project Structure

The project follows a feature-based Clean Architecture structure:

```text
com.dinzio.bookingapp
├── common                # Shared resources and base configurations
│   ├── navigation        # Route definitions and NavHost
│   ├── network           # Network-related helpers/interceptors
│   ├── theme             # Compose UI Theme (Color, Typography, etc.)
│   └── utils             # Extensions and common helper classes
├── core                  # Global core logic
│   ├── data              # Global data sources (Local & Remote)
│   └── di                # Global Dependency Injection modules
├── features              # Feature-based modules
│   ├── auth              # Authentication Feature
│   │   ├── data          # Repository impl, Data Sources, Models (DTOs)
│   │   ├── domain        # UseCases and Repository Interfaces
│   │   └── presentation  # UI Screens and ViewModels
│   ├── booking           # Booking Management Feature
│   └── main              # Dashboard/Main Feature
├── BaseApplication.kt    # Hilt Application class
└── MainActivity.kt       # Entry point activity
```

---

## Requirements
* Minimum SDK: 24 (Android 7.0 Nougat)
* Target SDK: 36
* Java Version: 11
* Kotlin Target: JVM 11
* Android Studio: Ladybug (2024.2.1) or newer

## Getting Started
### 1. Clone the repository
    git clone [https://github.com/yourusername/bookingapp.git](https://github.com/yourusername/bookingapp.git)

### 2. Setup Firebase
* Create a project in Firebase Console.
* Register the app with package name ```com.dinzio.bookingapp```.
* Download the ```google-services.json``` file.
* Place the file inside the ```/app``` directory of this project.

### 3. Build & Run
* Open the project in **Android Studio**.
* Wait for the **Gradle Sync** to finish.
* Select an emulator or a physical device.
* Click the **Run** (green play) button.

## Testing

You can run tests directly within Android Studio without using the command line:

### Unit Tests
* Navigate to the ```test``` folder (e.g., src/test/java).
* **Right-click** on the test class or folder.
* Select **'Run [ClassName]'** with the green play icon.
* These tests use **MockK**, **Turbine**, and **JUnit** to verify business logic.