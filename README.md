# Paper Trading Project (Android Studio)

Welcome to the Paper Trading Project! This project simulates a trading environment where users can practice trading without risking real money. It consists of two main components:

1. **Backend**: Implemented using Spring Boot, this component handles the core trading logic, user management, and data persistence.
2. **Frontend**: Implemented using Android Studio, this component provides a mobile interface for users to interact with the trading system.

## Repository Structure

- **Backend (Spring Boot)**: [Link to Spring Boot Repository](https://github.com/JatinNavani/PaperTrade_SpringBoot)
- **Frontend (Android Studio)**: [Link to Android Studio Repository](https://github.com/JatinNavani/PaperTrading)

## Prerequisites

- Android Studio 4.2 or higher
- Android SDK with API level 21 or higher

## Configuration and Setup

### 1. Configure Backend URL

Ensure the backend URL is correctly configured in your Android Studio project to communicate with the Spring Boot backend. Update the URLs in the following files:

- `InstrumentsUpdate.java`
- `BuySellActivity.java`
- `MainActivity.java`

Update the base URL for API requests in your application to point to the Spring Boot backend. Modify the relevant constants or configuration files in your Android project.

### 2. RabbitMQ Consumer Configuration

Ensure RabbitMQ consumer settings are configured properly in your Android application to receive messages from the backend. Update the username, password, and hostname according to your RabbitMQ server configuration.

### 3. Build and Run the Application

#### Import and Build the Project

1. Clone the repository and open it in Android Studio.
   
2. Open Android Studio and select `File` > `Open...`. Navigate to the cloned repository and select it.

3. Build the project by clicking on `Build` > `Make Project` or using the shortcut `Ctrl+F9`.

#### Run the Application

1. Run the application on an emulator or a physical device.

2. Select your target device or emulator from the dropdown menu in Android Studio.

3. Click on `Run` > `Run 'app'` or use the shortcut `Shift+F10`.

4. The app should launch on your device or emulator, connecting to the backend and RabbitMQ as configured.

---

### Additional Notes

- Ensure the backend server and RabbitMQ server are running and accessible before running the Android application to avoid connectivity issues.
