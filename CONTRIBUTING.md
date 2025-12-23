# Contributing to QKSMS

Thank you for your interest in contributing to QKSMS! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Building the Project](#building-the-project)
- [Project Architecture](#project-architecture)
- [Coding Guidelines](#coding-guidelines)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)
- [Translations](#translations)

## Code of Conduct

This project follows open source best practices. Please be respectful and constructive in all interactions with the community.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally: `git clone https://github.com/YOUR_USERNAME/qksms.git`
3. Create a new branch for your changes: `git checkout -b feature/your-feature-name`
4. Make your changes and commit them with clear commit messages
5. Push to your fork and submit a pull request

## Development Environment Setup

### Prerequisites

- **Android Studio** (latest stable version recommended, Arctic Fox or newer)
- **JDK 8 or higher** (JDK 11 recommended)
- **Git**
- **Android SDK** with:
  - API level 21+ (Android 5.0+) for minimum SDK
  - API level 34+ (Android 14) for compile SDK
  - Build Tools 34.0.0 or higher

### Initial Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/chartmann1590/qksms.git
   cd qksms
   ```

2. Open the project in Android Studio:
   - File → Open → Select the `qksms` directory
   - Wait for Gradle sync to complete

3. Create `local.properties` file in the project root (if not exists):
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   ```

4. For release builds, add signing configuration to `local.properties`:
   ```properties
   STORE_FILE=/path/to/keystore.jks
   STORE_PASSWORD=your_store_password
   KEY_ALIAS=your_key_alias
   KEY_PASSWORD=your_key_password
   ```

### Optional: AI Smart Reply Setup

To test AI Smart Reply features:

1. Install [Ollama](https://ollama.ai) on your development machine
2. Pull a model: `ollama pull llama3.2`
3. Start Ollama with network access: `OLLAMA_HOST=0.0.0.0 ollama serve`
4. Configure in app: Settings → Smart Reply (AI) → Enter your local IP (e.g., `http://192.168.1.100:11434`)

## Building the Project

### Build Variants

QKSMS has two product flavors:

- **noAnalytics** - Clean build without tracking (recommended for development and F-Droid)
- **withAnalytics** - Includes Firebase Analytics, Crashlytics, and Performance (for Google Play)

### Common Build Commands

```bash
# Build debug APK (no analytics - recommended for development)
./gradlew :presentation:assembleNoAnalyticsDebug

# Build debug APK (with analytics)
./gradlew :presentation:assembleWithAnalyticsDebug

# Install on connected device
./gradlew :presentation:installNoAnalyticsDebug

# Build release APK (requires signing config in local.properties)
./gradlew :presentation:assembleNoAnalyticsRelease
./gradlew :presentation:assembleWithAnalyticsRelease

# Build Android App Bundle for Play Store
./gradlew :presentation:bundleWithAnalyticsRelease

# Clean build
./gradlew clean

# Run lint checks (all modules)
./gradlew lint

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew :presentation:connectedNoAnalyticsDebugAndroidTest
```

**On Windows**, use `gradlew.bat` instead of `./gradlew`

## Project Architecture

QKSMS follows **Clean Architecture** with clear separation of concerns:

### Module Structure

1. **presentation/** - UI layer (Android app module)
   - Activities and Controllers using Conductor framework
   - MVP pattern with unidirectional state flow
   - Feature-based organization under `com.charles.messenger.feature`
   - Product flavors: `withAnalytics`, `noAnalytics`
   - Depends on: `domain`, `data`, `common`, `android-smsmms`

2. **domain/** - Business logic layer (pure Kotlin)
   - Use cases/Interactors (extending `Interactor<Params>`)
   - Repository interfaces
   - Domain models and business rules
   - No Android dependencies - only Kotlin, RxJava, and Realm
   - Depends on: `common`

3. **data/** - Data access layer (Android library)
   - Repository implementations
   - Database (Realm) and ContentProvider access for SMS/MMS
   - Data mappers between data models and domain models
   - Product flavors: `withAnalytics` (includes Amplitude), `noAnalytics`
   - Depends on: `domain`, `common`, `android-smsmms`

4. **common/** - Shared utilities (Android library)
   - Extension functions and null-safety helpers
   - Common models and utilities
   - DebugLogger utility for runtime debugging
   - No module dependencies

5. **android-smsmms/** - SMS/MMS handling library
   - Forked from klinker41/android-smsmms
   - Handles low-level SMS/MMS protocol operations
   - Used by other modules for telephony operations

### Key Patterns

- **MVP (Model-View-Presenter)** with unidirectional state flow
  - Views implement `QkView<State>` with `render(state: State)` method
  - Presenters extend `QkPresenter<View, State>` managing state with RxJava BehaviorSubject
  - State flow: User Intent → Presenter → State Reducer → View Render

- **Interactor Pattern**
  - All use cases extend `Interactor<Params>` from domain module
  - Execute on IO thread, observe on main thread
  - Located in `domain/src/main/java/com/charles/messenger/interactor/`

- **Dependency Injection** using Dagger 2
  - Activity/Service/BroadcastReceiver modules in `presentation/src/main/java/com/charles/messenger/injection/`
  - Scopes: `@ActivityScope`, `@ControllerScope`, `@ServiceScope`

- **Reactive Programming** using RxJava 2
  - Heavy use for asynchronous operations and data streams
  - Realm reactive queries for database observation
  - AutoDispose for lifecycle-aware subscription management

- **Database** using Realm
  - Fast, efficient local data persistence
  - Reactive queries using Realm's Flowable support
  - **IMPORTANT:** Realm plugin must be applied BEFORE Kotlin plugin in build.gradle

- **Navigation** using Conductor
  - Bluelinelabs Conductor library for Controller-based navigation
  - Lightweight, non-Activity view controllers
  - Located in feature modules (e.g., `feature/settings/about/AboutController.kt`)

For detailed architecture documentation, see [CLAUDE.md](CLAUDE.md).

## Coding Guidelines

### Language and Style

- **Language:** Kotlin (preferred), Java for legacy code only
- **Indentation:** 4 spaces (no tabs)
- **Encoding:** UTF-8
- **Line endings:** LF (Unix-style)
- **No wildcard imports**

### Naming Conventions

- **Classes/Objects:** `UpperCamelCase` (e.g., `MainActivity`, `MessageRepository`)
- **Functions/Variables:** `lowerCamelCase` (e.g., `sendMessage`, `conversationId`)
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `MAX_MESSAGE_LENGTH`, `DEFAULT_TIMEOUT`)
- **Resources:** `lower_snake_case` (e.g., `activity_main.xml`, `ic_send.xml`, `string key: main_title`)

### Code Organization

- Prefer extension functions for utility methods (see `common/util/extensions`)
- Use Kotlin null-safety features (`?.`, `?:`, `!!` sparingly)
- Keep functions small and focused (single responsibility)
- Use data classes for models and state objects
- Leverage sealed classes for representing states

### Important Notes

- **Realm Plugin Ordering:** Always apply Realm plugin BEFORE Kotlin plugin in `build.gradle`:
  ```gradle
  apply plugin: 'realm-android'  // Must be first
  apply plugin: 'kotlin-android'
  ```

## Testing

### Running Tests

```bash
# Run all unit tests (where present)
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew :presentation:connectedNoAnalyticsDebugAndroidTest

# Run specific module tests
./gradlew :domain:test
./gradlew :data:test
```

**Note:** Most tests are instrumented tests located in `*/src/androidTest/java/...` as Kotlin `*Test.kt` files. Unit tests are less common but may exist in some modules.

### Test Guidelines

- Place instrumented tests in `src/androidTest/java/`
- Name test files with `*Test.kt` suffix
- Use AndroidX Test, Espresso, and Mockito frameworks
- Write clear, descriptive test names that explain what's being tested
- Include test coverage for new features and bug fixes

### Test Plan for Pull Requests

When submitting a PR, include:
- Which build variant(s) you tested
- Device/emulator specifications
- Steps to verify the changes
- Screenshots or screen recordings for UI changes

## Submitting Changes

### Commit Messages

Follow these guidelines for commit messages:

- Use imperative present tense ("Add feature" not "Added feature")
- Keep the first line concise (50 characters or less)
- Add detailed description after a blank line if needed
- Reference issues and PRs when relevant (e.g., "Fix #123")
- Prefix with module name when applicable (e.g., `presentation: Fix crash in compose screen`)

**Good examples:**
```
Add AI Smart Reply feature with Ollama integration

Implements smart reply suggestions using local Ollama server.
Users can configure their server URL and select models.

Fixes #456
```

```
domain: Refactor message sending interactor

Simplify error handling and improve testability.
```

### Pull Request Process

1. **Update your branch** with the latest changes from `master`:
   ```bash
   git checkout master
   git pull upstream master
   git checkout your-feature-branch
   git rebase master
   ```

2. **Ensure all tests pass** and the app builds successfully

3. **Create a pull request** with:
   - Clear title describing the change
   - Description of what changed and why
   - Screenshots/videos for UI changes
   - Test plan (how to verify the changes)
   - Reference to related issues

4. **Address review feedback** promptly and courteously

5. **Squash commits** if requested before merging

### Pull Request Template

```markdown
## Summary
Brief description of changes

## Changes
- Change 1
- Change 2

## Screenshots/Videos
(if applicable)

## Test Plan
1. Build variant tested: `noAnalytics`/`withAnalytics`
2. Device/emulator: [device info]
3. Steps to verify:
   - Step 1
   - Step 2

## Related Issues
Fixes #123
```

## Reporting Bugs

Before submitting a bug report:

1. **Search existing issues** to avoid duplicates
2. **Use the latest release** to verify the bug still exists
3. **Gather information:**
   - QKSMS version
   - Android version and device model
   - Build variant (WithAnalytics/NoAnalytics)
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots or logs if applicable

Create a new issue with a clear title and detailed description.

## Feature Requests

Feature requests are welcome! When submitting a feature request:

1. **Check existing issues** to see if it's already been suggested
2. **Describe the use case** - why would this feature be useful?
3. **Provide examples** or mockups if possible
4. **Consider the scope** - does it fit within QKSMS's goals?

## Translations

Translations are managed through **Crowdin**. To contribute translations:

1. Visit https://crowdin.com/project/qksms
2. Select your language
3. Start translating!

**Do not** modify string resources directly in the codebase - all translations should go through Crowdin to maintain consistency.

## Security & Configuration

### Important Security Practices

- **Never commit secrets, API keys, or keystores** to the repository
- Use `local.properties` for sensitive configuration:
  ```properties
  STORE_FILE=/path/to/keystore.jks
  STORE_PASSWORD=your_password
  KEY_ALIAS=your_alias
  KEY_PASSWORD=your_key_password
  ADMOB_APP_ID=your_admob_id
  ADMOB_BANNER_ID=your_banner_id
  ```
- For analytics keys, use environment variables or build configuration
- Review `.gitignore` to ensure sensitive files are excluded

### Choosing Build Flavors

- Use **noAnalytics** for development and testing (faster, cleaner)
- Use **withAnalytics** when testing Firebase integration or preparing releases for Google Play

## Questions?

If you have questions about contributing:

- **Email:** tickets@portfolio-8ul8id.p.tawk.email
- **GitHub Issues:** https://github.com/chartmann1590/qksms/issues

## License

By contributing to QKSMS, you agree that your contributions will be licensed under the GNU General Public License v3.0 (GPLv3).

---

**Thank you for contributing to QKSMS!**
