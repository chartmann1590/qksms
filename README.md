![TextPilot AI Messaging](https://user-images.githubusercontent.com/4358785/39079306-a5a409b6-44e5-11e8-8589-b4acd63b636e.jpg)

# TextPilot AI Messaging - The Beautiful Open Source SMS App with AI

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Download](https://img.shields.io/github/v/release/chartmann1590/textpilot)](https://github.com/chartmann1590/textpilot/releases/latest)
[![Version](https://img.shields.io/badge/version-3.10.2-brightgreen.svg)](https://github.com/chartmann1590/textpilot/releases)

**TextPilot AI Messaging** is a powerful, feature-rich open source SMS and MMS messaging application for Android. Originally forked from [QKSMS](https://github.com/moezbhatti/qksms), this enhanced version adds cutting-edge AI capabilities and numerous improvements while maintaining the beautiful, customizable experience of the original.

## Download & Install

Get TextPilot AI Messaging on your Android device:

**[📱 Download from Google Play Store](https://play.google.com/store/apps/details?id=com.charles.messenger.v2)**

**[📦 Download APK from GitHub Releases](https://github.com/chartmann1590/textpilot/releases/latest)**

## What's New in TextPilot AI Messaging

This fork includes significant enhancements and new features beyond the original QKSMS:

### 🤖 AI-Powered Features (NEW!)

- **AI Smart Reply** - Get intelligent reply suggestions powered by your own local Ollama server
  - Smart reply suggestions while composing messages
  - Auto-Reply to All Messages - Let AI automatically respond when you're busy
  - Local AI Processing - Uses Ollama running on your own machine—your messages never leave your network
  - Multiple Model Support - Choose from any model installed on your Ollama server
  - Custom AI Persona - Define how the AI should respond
  - AI Signature Support - Automatically append signatures to AI-generated replies

**Setup Instructions:**
1. Install [Ollama](https://ollama.ai) on your computer and pull a model (e.g., `ollama pull llama3.2`)
2. Start Ollama with network access: `OLLAMA_HOST=0.0.0.0 ollama serve`
3. In TextPilot, go to **Settings → Smart Reply (AI)**
4. Enter your Ollama API URL (e.g., `http://192.168.1.100:11434` for your PC's local IP)
5. Tap **Test Connection** to verify connectivity
6. Select your preferred AI model from the dropdown
7. Enable **Smart Reply** for suggestions, or **Auto-Reply to All** for fully automatic responses

**Note:** Auto-Reply shows a persistent notification so you can quickly disable it. Use responsibly!

### 🌐 Web SMS Sync (NEW! - In Development)

**TextPilot Web Interface** - Access and manage your SMS/MMS messages from any web browser! This self-hosted solution provides a secure, real-time interface that syncs with your Android device.

**Status:** The Web SMS feature is currently in active development. The backend infrastructure is complete and functional, with the web client interface being continuously improved. **Ready for users to try!**

#### Key Features

- 🔒 **Self-hosted** - Complete control over your messaging data
- 🔐 **Secure** - JWT authentication with encrypted credentials and rate limiting
- ⚡ **Real-time Sync** - Instant sync when messages are sent/received, plus automatic backup every minute
- 📱 **Two-way sync** - Send and receive messages from both phone and web
- 🖼️ **MMS Support** - View and send multimedia messages with attachments
- 🐳 **Easy deployment** - One-command Docker Compose setup
- 📊 **Modern UI** - Clean, Material Design-inspired React web interface
- 🔄 **Automatic sync** - Background service syncs messages every minute, with instant sync on send/receive

#### How It Works

1. **Server Setup**: Deploy the self-hosted web server using Docker Compose (one command!)
2. **Initial Sync**: When you first enable Web Sync in TextPilot, all conversations and messages are uploaded to your server in batches
3. **Incremental Sync**: New messages are automatically synced instantly when sent/received, plus periodic backup every minute
4. **Web Access**: Access your messages from any web browser with a modern, responsive interface
5. **Two-Way Communication**: Send messages from the web interface, and they're automatically queued and sent by your phone

#### Sync Mechanism

- **Instant Sync**: Automatically triggered when messages are sent or received on your phone
- **Periodic Sync**: Background service runs every 1 minute to ensure all messages are synced
- **Queue System**: Messages sent from the web are queued on the server and picked up by the Android app during sync
- **Batch Processing**: Large message histories are synced in batches of 100 messages for efficiency

#### Quick Start

1. **Set up the server** (see [Web SMS Documentation](docs/WEB_SMS_FEATURE.md) for detailed instructions):
   ```bash
   cd web-interface
   cp .env.example .env
   # Edit .env with your configuration (generate JWT secrets)
   docker-compose up -d
   ```

2. **Configure in TextPilot**:
   - Open TextPilot app
   - Go to **Settings → Web Sync**
   - Enable **Web Sync**
   - Enter your server URL (e.g., `http://192.168.1.100:8081`)
   - Create a username and password
   - Tap **Test Connection** to verify connectivity
   - Once connected, tap **Perform Initial Sync**

3. **Access the web interface**:
   - Open your browser
   - Navigate to your server URL
   - Log in with your credentials
   - Start managing your messages from the web!

#### Current Implementation Status

- ✅ **Backend Server** - Fully functional REST API with authentication (Node.js/Express)
- ✅ **Database** - PostgreSQL with TypeORM for message storage
- ✅ **Real-time Sync** - WebSocket support (Socket.io) for instant updates
- ✅ **Android Integration** - Complete sync support with background service
- ✅ **Web Client** - React/TypeScript interface with Redux state management
- ✅ **Docker Deployment** - One-command setup with Docker Compose
- ✅ **Security** - JWT authentication, bcrypt password hashing, rate limiting, CORS protection
- ✅ **MMS Support** - Full attachment handling and media support
- ✅ **Queue System** - Web-sent messages queued and sent by Android app
- ✅ **Background Service** - Automatic periodic sync every minute
- ✅ **Instant Sync** - Triggers immediately on message send/receive
- 🔄 Message search (coming soon)
- 🔄 Contact photos (coming soon)
- 🔄 Enhanced UI improvements (ongoing)

#### Documentation

For complete setup instructions, architecture details, API documentation, troubleshooting, and more, see the **[Web SMS Feature Documentation](docs/WEB_SMS_FEATURE.md)**.

### Enhanced Features & Improvements

- **Enhanced Permission Management** - Improved handling for Android 13+ (Tiramisu) and Android 14+ (API 34+)
  - Foreground service permission support for Android 14+
  - Enhanced notification permission checking
  - Better permission validation across all permission types

- **Improved Notification System** - Enhanced notification channel management
  - Better handling of backup/restore notification channels
  - Improved notification permission checks
  - Enhanced notification building with proper PendingIntent handling
  - Better support for group conversations

- **UI/UX Enhancements** - Significant improvements to the user interface
  - Window insets handling for better system bar compatibility
  - Support for edge-to-edge displays
  - Enhanced toolbar binding and title management
  - Improved base activity classes for better theming support
  - Better integration with Material Design components

- **QK Reply Improvements** - Enhanced quick reply functionality
  - Improved UI handling
  - Better integration with notification system
  - Enhanced state management
  - Improved reactive programming patterns

- **Performance & Stability** - Ongoing improvements
  - Performance optimizations
  - Stability updates
  - Bug fixes and improvements
  - Better error handling throughout the codebase

## Why Choose TextPilot AI Messaging?

- **100% Free and Open Source** - Released under GPLv3, fully transparent and community-driven
- **🤖 AI-Powered** - Smart reply suggestions and auto-reply using your own local Ollama server for complete privacy
- **🌐 Web SMS Sync** - Access your messages from any web browser with self-hosted web interface (in development, ready to try!)
- **Beautiful Material Design** - Modern, clean interface that follows Material Design principles
- **Highly Customizable** - Make it truly yours with extensive theming and customization options
- **Privacy Focused** - Your messages stay on your device, with optional local backups and self-hosted web sync
- **Feature Rich** - Everything you'd expect from a messaging app, plus powerful extras
- **No Ads or Tracking** - Clean experience without intrusive advertisements (NoAnalytics variant available)
- **Based on QKSMS** - Built on the solid foundation of the original QKSMS with significant enhancements

## Core Features

### Messaging Essentials
- **SMS and MMS Support** - Send text messages, photos, videos, and group messages
- **Dual SIM Support** - Seamlessly switch between multiple SIM cards
- **Group Messaging** - Create and manage group conversations with ease
- **Delivery Confirmations** - Know when your messages are delivered
- **Message Search** - Quickly find conversations and specific messages
- **Contact Cards** - Share vCard contact information
- **Media Gallery** - View and save photos and videos from conversations

### Beautiful Interface
- **Material Design** - Clean, modern interface following Google's design guidelines
- **Customizable Themes** - Choose from Material Design color palette
- **Premium Themes** - Unlock beautiful exclusive colors with Messenger+ (optional in-app purchase)
- **Night Mode** - Easy on the eyes with dark theme
- **Automatic Night Mode** - Schedule night mode based on time of day (Messenger+ feature)
- **Pure Black Mode** - Perfect for AMOLED screens
- **Automatic Contact Colors** - Unique colors for each conversation
- **Custom Font Size** - Adjust text size to your preference
- **System Font Support** - Use your device's system font

### Smart Organization
- **Archive Conversations** - Keep your inbox clean without deleting
- **Pin Conversations** - Keep important chats at the top
- **Mark as Read/Unread** - Manage conversation states
- **Customizable Swipe Actions** - Configure left and right swipes (Archive, Delete, Block, Call, Mark Read/Unread)
- **Auto-Delete Old Messages** - Automatically remove messages after a specified number of days
- **Conversation Details** - View message metadata including delivery status and timestamps

### Advanced Features

#### QK Reply - Smart Popup
- Popup notifications for new messages
- Reply without leaving your current app
- Quick actions: Mark read, Call, Delete, Expand/Collapse
- Tap to dismiss option

#### Blocking & Spam Protection
- **Built-in Message Blocking** - Block unwanted numbers and conversations
- **Drop Blocked Messages** - Completely prevent blocked messages from saving
- **View Blocked Messages** - See what you've blocked (if not dropped)
- **Third-Party Integration** - Compatible with Call Blocker, Call Control, and Should I Answer apps
- **Advanced Blocking** (Messenger+ feature) - Block messages containing specific keywords or patterns

#### Message Scheduling & Timing
- **Scheduled Messages** (Messenger+ feature) - Send messages at a specific date and time
- **Delayed Sending** (Messenger+ feature) - Add a few seconds delay before sending
- **Schedule Management** - View and manage all scheduled messages

#### Backup & Restore
- **Local Backup** (Messenger+ feature) - Back up your messages to your device
- **Easy Restore** - Restore messages from backup files
- **Manual Backups** - Create backups anytime you want
- Never worry about losing your conversation history

#### Smart Automation (Messenger+ Features)
- **Auto-Forward** - Automatically forward messages from specific senders
- **Auto-Respond** - Set up automatic replies to incoming messages
- **Custom Auto-Emoji** - Create custom emoji shortcuts

### Notifications & Customization
- **Customizable Notifications** - Full control over notification appearance
- **Three Notification Action Buttons** - Quick reply, mark read, and more
- **Notification Previews** - Choose whether to show message content
- **Wake Screen** - Screen lights up for new messages
- **Custom Vibration** - Set vibration patterns
- **Custom Ringtones** - Choose notification sounds per conversation
- **Per-Conversation Notifications** - Different settings for each chat
- **Per-Conversation Themes** - Unique colors for each conversation

### Additional Features
- **Message Signatures** - Add a signature to your outgoing messages
- **Strip Accents** - Remove accents from characters in SMS messages
- **Mobile Numbers Only** - Filter to show only mobile numbers when composing
- **Long SMS as MMS** - Option to send long text messages as MMS
- **Auto-Compress MMS** - Automatically resize MMS attachments
- **Message Sync** - Re-sync with Android's native SMS database
- **Home Screen Widget** - View conversations directly from your home screen
- **Automatic Emoji** - Convert text shortcuts to emoji automatically
- **Web SMS Sync** - Self-hosted web interface to access messages from any browser (see [Web SMS Documentation](docs/WEB_SMS_FEATURE.md))

## Messenger+ (Optional Premium Features)

Support development and unlock premium features with a one-time purchase:

- Premium themes with exclusive colors
- Scheduled messages
- Delayed sending
- Automatic night mode scheduling
- Message backup and restore
- Advanced blocking (keywords and patterns)
- Auto-forward messages
- Auto-respond to messages
- Custom auto-emoji shortcuts
- All future Messenger+ features

## Technology & Architecture

TextPilot AI Messaging is built with modern Android development practices:

- **Clean Architecture** - Separation of concerns across presentation, domain, and data layers
- **MVP Pattern** - Unidirectional state flow for predictable UI updates
- **Reactive Programming** - RxJava 2 for asynchronous operations
- **Dependency Injection** - Dagger 2 for modular, testable code
- **Realm Database** - Fast, efficient local data storage
- **Conductor Framework** - Lightweight view controllers for smooth navigation
- **Kotlin** - Primary language with Java interop for legacy code
- **Material Design Components** - Modern UI components following Material Design guidelines
- **Ollama Integration** - Local AI processing with Ollama API
- **Web Sync Architecture** - Node.js/Express backend, React/TypeScript frontend, PostgreSQL database, WebSocket real-time communication

### Module Structure

- **presentation/** - Android app module (UI, activities, controllers, resources)
- **domain/** - Business logic layer (pure Kotlin, no Android dependencies)
- **data/** - Data access layer (repositories, database, networking)
- **common/** - Shared utilities, extensions, and debugging tools
- **android-smsmms/** - SMS/MMS handling library
- **web-interface/** - Self-hosted web interface for SMS/MMS management (server, client, Docker setup)

### Build Variants

TextPilot AI Messaging is available in two product flavors:

- **withAnalytics** - Includes Firebase Analytics, Crashlytics, Performance monitoring, Google Play Billing, and Amplitude (for Google Play Store)
- **noAnalytics** - Clean build without any tracking or analytics (for F-Droid and privacy-focused users)

The `noAnalytics` variant is still being updated to match the `withAnalytics` version, but anyone is welcome to fork and work on it themselves. The `noAnalytics` variant is recommended for development and testing as it builds faster and has no external dependencies.

## Building from Source

### Prerequisites

- **Android Studio** (latest stable version recommended)
- **JDK 8 or higher**
- **Android SDK** with API level 21+ (Android 5.0+)
- **Git**

### Build Commands

```bash
# Clone the repository
git clone https://github.com/chartmann1590/textpilot.git
cd textpilot

# Build debug APK without analytics (F-Droid variant - recommended for development)
./gradlew :presentation:assembleNoAnalyticsDebug

# Build debug APK with analytics (Google Play variant)
./gradlew :presentation:assembleWithAnalyticsDebug

# Install on connected device
./gradlew :presentation:installNoAnalyticsDebug

# Build release APK (requires signing configuration in local.properties)
./gradlew :presentation:assembleNoAnalyticsRelease
./gradlew :presentation:assembleWithAnalyticsRelease

# Build Android App Bundle for Play Store
./gradlew :presentation:bundleWithAnalyticsRelease

# Run all tests
./gradlew test

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean
```

**On Windows**, use `gradlew.bat` instead of `./gradlew`

### Signing Configuration

For release builds, create a `local.properties` file in the project root with:

```properties
STORE_FILE=/path/to/keystore.jks
STORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed build instructions and [CLAUDE.md](CLAUDE.md) for architecture documentation.

## Contributing

Contributions are welcome! This is an open source project under active development.

### Translations

Translations are managed through Crowdin. Help translate TextPilot AI Messaging into your language:
https://crowdin.com/project/qksms

### Bug Reports

Found a bug? Please ensure you're using the latest release and [open an issue](https://github.com/chartmann1590/textpilot/issues) with details.

## Contact & Support

- **Developer:** Charles Hartmann
- **Email:** tickets@portfolio-8ul8id.p.tawk.email
- **Source Code:** https://github.com/chartmann1590/textpilot
- **Changelog:** https://github.com/chartmann1590/textpilot/releases
- **Google Play Store:** https://play.google.com/store/apps/details?id=com.charles.messenger.v2

## License

TextPilot AI Messaging is released under the **GNU General Public License v3.0 (GPLv3)**.

This means you're free to:
- Use the app for any purpose
- Study and modify the source code
- Share the app with others
- Share your modifications

See the [LICENSE](LICENSE) file for the full license text.

---

**Made with ❤️ by the open source community**

*TextPilot AI Messaging is a fork of [QKSMS](https://github.com/moezbhatti/qksms) with significant enhancements including AI-powered features, Web SMS sync capability, improved permissions handling, enhanced notifications, and numerous UI/UX improvements.*

*Love the app? Consider supporting development by unlocking Messenger+ features or sharing TextPilot AI Messaging with your friends!*
