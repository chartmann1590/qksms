![QKSMS](https://user-images.githubusercontent.com/4358785/39079306-a5a409b6-44e5-11e8-8589-b4acd63b636e.jpg)

# QKSMS - The Beautiful Open Source SMS App

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Download](https://img.shields.io/github/v/release/chartmann1590/qksms)](https://github.com/chartmann1590/qksms/releases/latest)

QKSMS is a powerful, feature-rich open source SMS and MMS messaging application for Android that replaces your stock messaging app with a beautiful, customizable experience. Built with clean architecture and modern Android development practices, QKSMS offers everything you need from a messaging app and more.

## Download

Get the latest version of QKSMS:

**[Download APK from GitHub Releases](https://github.com/chartmann1590/qksms/releases/latest)**

## Why Choose QKSMS?

- **100% Free and Open Source** - Released under GPLv3, fully transparent and community-driven
- **Beautiful Material Design** - Modern, clean interface that follows Material Design principles
- **Highly Customizable** - Make it truly yours with extensive theming and customization options
- **Privacy Focused** - Your messages stay on your device, with optional local backups
- **Feature Rich** - Everything you'd expect from a messaging app, plus powerful extras
- **No Ads or Tracking** - Clean experience without intrusive advertisements (NoAnalytics variant available)

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

**Note:** F-Droid users get Messenger+ features free! Optional PayPal donations welcome to support development.

## Technology & Architecture

QKSMS is built with modern Android development practices:

- **Clean Architecture** - Separation of concerns across presentation, domain, and data layers
- **MVP Pattern** - Unidirectional state flow for predictable UI updates
- **Reactive Programming** - RxJava 2 for asynchronous operations
- **Dependency Injection** - Dagger 2 for modular, testable code
- **Realm Database** - Fast, efficient local data storage
- **Conductor Framework** - Lightweight view controllers for smooth navigation

### Build Variants

- **WithAnalytics** - Includes Firebase Analytics, Crashlytics, and Performance monitoring (Google Play)
- **NoAnalytics** - Clean build without any tracking for privacy-focused users (F-Droid)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/chartmann1590/qksms.git
cd qksms

# Build debug APK
./gradlew assembleDebug

# Build without analytics (F-Droid variant)
./gradlew :presentation:assembleNoAnalyticsDebug

# Build with analytics (Google Play variant)
./gradlew :presentation:assembleWithAnalyticsDebug

# Run tests
./gradlew test
```

See [CLAUDE.md](CLAUDE.md) for detailed build instructions and architecture documentation.

## Contributing

Contributions are welcome! This is an open source project under active development.

### Translations

Translations are managed through Crowdin. Help translate QKSMS into your language:
https://crowdin.com/project/qksms

### Bug Reports

Found a bug? Please ensure you're using the latest release and [open an issue](https://github.com/chartmann1590/qksms/issues) with details.

## Contact & Support

- **Developer:** Charles Hartmann
- **Email:** tickets@portfolio-8ul8id.p.tawk.email
- **Source Code:** https://github.com/chartmann1590/qksms
- **Changelog:** https://github.com/chartmann1590/qksms/releases

## License

QKSMS is released under the **GNU General Public License v3.0 (GPLv3)**.

This means you're free to:
- Use the app for any purpose
- Study and modify the source code
- Share the app with others
- Share your modifications

See the [LICENSE](LICENSE) file for the full license text.

---

**Made with ❤️ by the open source community**

*Love the app? Consider supporting development by unlocking Messenger+ features or sharing QKSMS with your friends!*
