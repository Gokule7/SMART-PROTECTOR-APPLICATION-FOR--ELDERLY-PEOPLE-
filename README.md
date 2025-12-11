# Smart Protector Application for Elderly People

A comprehensive Android application system designed to ensure the safety and well-being of elderly people through smart monitoring, fall detection, medication reminders, and caretaker connectivity.

## Overview

This project consists of two main components:
- **SPA (Smart Protector Application)**: Main mobile application for both elderly users and their caretakers
- **SPAWearable**: Companion wearable OS application for enhanced monitoring and fall detection

## Features

### For Elderly Users
- 🏥 **Health Monitoring**: Track vital health metrics and appointments
- 💊 **Medication Reminders**: Automated reminders for medication schedules
- 📅 **Check-up Reminders**: Never miss important medical appointments
- 🚨 **Emergency Alerts**: Quick access to emergency services
- 🤝 **Caretaker Connection**: Stay connected with designated caretakers
- 📱 **Fall Detection**: Automatic fall detection using device sensors (via wearable)

### For Caretakers
- 👥 **Elder Management**: Connect and monitor multiple elderly users
- 🔔 **Real-time Notifications**: Receive instant alerts for emergencies and fall detection
- 📊 **Health Tracking**: View medication schedules and check-up appointments
- ⚙️ **Reminder Setup**: Set and manage reminders for connected elderly users
- 📍 **Status Monitoring**: Check the well-being status of elderly users

## Technology Stack

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Backend**: Firebase
  - Firebase Authentication
  - Cloud Firestore
  - Firebase Cloud Messaging (FCM)
  - Google Services
- **UI Framework**: 
  - Android Views (SPA)
  - Jetpack Compose (SPAWearable)
- **Architecture Components**:
  - Data Binding
  - ViewModel
  - LiveData
- **Sensors**: Accelerometer, Gyroscope for fall detection
- **Notifications**: FCM for real-time alerts

## Project Structure

```
Mobile_Hackthon_project/
├── SPA/                          # Main mobile application
│   └── SPA/
│       ├── app/
│       │   └── src/
│       │       └── main/
│       │           └── java/com/example/spa/
│       │               ├── caretakers/       # Caretaker-specific features
│       │               ├── elders/           # Elder-specific features
│       │               ├── connected/        # Connection management
│       │               ├── reminders/        # Reminder system
│       │               ├── services/         # Background services
│       │               ├── home/             # Entry and splash screens
│       │               └── utils/            # Helper classes and utilities
│       └── build.gradle.kts
│
└── SPAWearable/                  # Wearable companion app
    └── SPAWearable/
        ├── app/
        │   └── src/
        │       └── main/
        │           └── java/com/raju/spawearable/
        │               └── presentation/
        │                   ├── screens/      # UI screens
        │                   ├── services/     # Sensor services
        │                   ├── notifications/# FCM integration
        │                   └── utils/        # Utilities
        └── build.gradle.kts
```

## Setup Instructions

### Prerequisites
- Android Studio (Arctic Fox or later)
- JDK 11 or higher
- Android SDK (API 21+)
- Firebase account

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Gokule7/SMART-PROTECTOR-APPLICATION-FOR--ELDERLY-PEOPLE-.git
   cd SMART-PROTECTOR-APPLICATION-FOR--ELDERLY-PEOPLE-
   ```

2. **Configure Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add Android apps for both SPA and SPAWearable
   - Download `google-services.json` files and place them in respective `app/` directories
   - Enable Firebase Authentication, Firestore, and Cloud Messaging

3. **Open in Android Studio**
   - Open `SPA/SPA` for the main application
   - Open `SPAWearable/SPAWearable` for the wearable app

4. **Build the projects**
   ```bash
   # For SPA
   cd SPA/SPA
   ./gradlew build
   
   # For SPAWearable
   cd SPAWearable/SPAWearable
   ./gradlew build
   ```

5. **Run the applications**
   - Connect your Android device or start an emulator
   - Run the SPA application on a mobile device
   - Run the SPAWearable application on a Wear OS device or emulator

## Key Components

### Authentication System
- Phone number-based OTP authentication
- Separate sign-in flows for elderly users and caretakers
- Secure user profile management

### Fall Detection Service
- Real-time sensor monitoring on wearable devices
- Automatic emergency alert triggering
- Notification to all connected caretakers

### Reminder System
- Medication reminders with customizable schedules
- Medical check-up reminders
- Push notifications via FCM

### Connection Management
- QR code-based or ID-based elder-caretaker pairing
- Support for multiple caretakers per elderly user
- Real-time status updates

## Security & Privacy

- Firebase Authentication for secure user management
- Encrypted data storage in Cloud Firestore
- Privacy-focused design with consent-based monitoring
- Secure communication channels for notifications

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Developers

Developed as part of a Mobile Hackathon project focused on elderly care and safety.

## Acknowledgments

- Firebase for backend infrastructure
- Android Jetpack for modern Android development
- Material Design for UI components

## Support

For support, please open an issue in the GitHub repository or contact the development team.

---

**Note**: This application is designed to assist in elderly care but should not replace professional medical advice or emergency services. Always consult healthcare professionals for medical concerns.
