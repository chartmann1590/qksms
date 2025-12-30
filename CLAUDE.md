# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

QKSMS is an open source SMS/MMS messaging application for Android, released under GPLv3. It replaces the stock Android messaging app with enhanced features and customization.

## Build Commands

### Standard Build
```bash
# Build debug APK without analytics (F-Droid variant - recommended for development)
./gradlew :presentation:assembleNoAnalyticsDebug

# Build debug APK with analytics (Google Play variant)
./gradlew :presentation:assembleWithAnalyticsDebug

# Install on connected device
./gradlew :presentation:installNoAnalyticsDebug

# Build release APK (requires signing config in local.properties)
./gradlew :presentation:assembleNoAnalyticsRelease
./gradlew :presentation:assembleWithAnalyticsRelease

# Build Android App Bundle for Play Store
./gradlew :presentation:bundleWithAnalyticsRelease
```

### Testing
```bash
./gradlew test                                   # Run all unit tests (where present)
./gradlew :presentation:connectedNoAnalyticsDebugAndroidTest  # Run instrumentation tests (requires device/emulator)
./gradlew :domain:test                           # Run tests for domain module only
./gradlew :data:test                             # Run tests for data module only
```

**Note:** Most tests are instrumented tests located in `*/src/androidTest/java/...` as Kotlin `*Test.kt` files. Unit tests are less common.

### Other Commands
```bash
./gradlew androidDependencies                    # Download all Android dependencies
./gradlew clean                                  # Clean all build outputs
./gradlew lint                                   # Run lint checks on all modules
```

**On Windows**, use `gradlew.bat` instead of `./gradlew`

## Architecture

QKSMS follows **Clean Architecture** with clear separation of concerns across multiple Gradle modules:

### Module Structure

1. **presentation** - UI layer (Android app module)
   - Activities and Controllers (using Bluelinelabs Conductor for navigation)
   - Presenters implementing MVP pattern with unidirectional state flow
   - Feature-based organization under `com.charles.messenger.feature`
   - Depends on: `domain`, `data`, `common`, `android-smsmms`

2. **domain** - Business logic layer (pure Kotlin)
   - Use cases/Interactors (classes extending `Interactor<Params>`)
   - Repository interfaces
   - Domain models and business rules
   - No Android dependencies - only Kotlin, RxJava, and Realm
   - Depends on: `common`

3. **data** - Data access layer (Android library)
   - Repository implementations
   - Data sources (Realm database, ContentProviders for SMS/MMS)
   - Mappers between data models and domain models
   - Android-specific implementations
   - Product flavors: `withAnalytics` (includes Amplitude) and `noAnalytics`
   - Depends on: `domain`, `common`, `android-smsmms`

4. **common** - Shared utilities (Android library)
   - Common models and utilities shared across modules
   - DebugLogger utility for runtime debugging and logging
   - No module dependencies

5. **android-smsmms** - Third-party library for SMS/MMS handling
   - Forked from klinker41/android-smsmms
   - Handles low-level SMS/MMS protocol operations

### Key Architectural Patterns

**MVP with Unidirectional State Flow:**
- Views implement `QkView<State>` with a single `render(state: State)` method
- Presenters extend `QkPresenter<View, State>` and manage state using RxJava BehaviorSubject
- State changes flow: User Intent → Presenter → State Reducer → View Render
- Views can be Activities or Controllers (Conductor framework)

**Interactor Pattern:**
- All use cases extend `Interactor<Params>` from the domain module
- Execute business logic on IO thread, observe on main thread
- Located in `domain/src/main/java/com/charles/messenger/interactor/`

**Dependency Injection:**
- Dagger 2 for dependency injection
- Activity/Service/BroadcastReceiver modules in `presentation/src/main/java/com/charles/messenger/injection/`
- Scopes: `@ActivityScope`, `@ControllerScope`, `@ServiceScope`

**Navigation:**
- Uses Bluelinelabs Conductor library for Controller-based navigation
- Controllers are lightweight, non-Activity view controllers
- Located in feature modules (e.g., `feature/settings/about/AboutController.kt`)

**Reactive Programming:**
- Heavy use of RxJava 2 for asynchronous operations and data streams
- Realm reactive queries for database observation
- AutoDispose for lifecycle-aware subscription management

**Database:**
- Realm database for local data persistence
- Reactive queries using Realm's Flowable support
- IMPORTANT: Realm must be applied BEFORE Kotlin plugin in build.gradle or build will fail

### Product Flavors

Two build variants exist for different distribution channels:

- **withAnalytics**: Includes Firebase Crashlytics, Analytics, Performance, Google Play Billing, and Amplitude (for Google Play Store)
- **noAnalytics**: Clean build without tracking or analytics (for F-Droid and privacy-focused users)

Flavor configuration is in `presentation/build.gradle` and `data/build.gradle`.

**Note:** The `noAnalytics` variant is recommended for development as it builds faster and has no external dependencies.

### Feature Module Organization

Each feature in `presentation/src/main/java/com/charles/messenger/feature/` is organized as:
- Activity/Controller - View implementation
- Presenter - State management and business logic orchestration
- ViewModel/State data classes
- Dagger injection modules (e.g., `ComposeActivityModule.kt`)

Major features:
- `compose/` - Message composition screen with AI Smart Reply integration
- `conversations/` - Conversation list with swipe actions
- `main/` - Main app container and navigation drawer
- `blocking/` - Number blocking management (numbers, keywords, patterns)
- `settings/` - App settings and preferences (including AI Smart Reply settings)
- `plus/` - Premium features and billing (Messenger+)
- `backup/` - Local backup and restore functionality
- `scheduled/` - Scheduled message management
- `qkreply/` - QK Reply popup notifications
- `gallery/` - Media gallery viewer
- `contacts/` - Contact selection and management
- `conversationinfo/` - Conversation details and metadata
- `notificationprefs/` - Per-conversation notification preferences
- `themepicker/` - Theme selection and customization
- `widget/` - Home screen widget

## Development Notes

### Realm Plugin Ordering
Always apply the Realm plugin BEFORE Kotlin plugin in module build.gradle files:
```gradle
apply plugin: 'realm-android' // Must be before Kotlin
apply plugin: 'kotlin-android'
```

### Testing Product Flavors
To work with a specific flavor during development, use flavor-specific tasks:
```bash
./gradlew :presentation:assembleNoAnalyticsDebug    # F-Droid variant
./gradlew :presentation:assembleWithAnalyticsDebug  # Play Store variant
```

### Database Schema Changes
When modifying Realm models:
1. Update model classes in `domain/src/main/java/com/charles/messenger/model/`
2. Increment schema version in RealmMigration
3. Add migration logic in `data/src/main/java/com/charles/messenger/migration/`

### Key Dependencies

- **Kotlin**: 1.8.22
- **AndroidX**: AppCompat 1.1.0, Core KTX 1.1.0, Material 1.0.0
- **RxJava**: 2.1.4 (with RxAndroid 2.0.1, RxKotlin 2.1.0)
- **Dagger**: 2.16
- **Realm**: 10.11.1
- **Conductor**: 2.1.5
- **Glide**: 4.8.0
- **ExoPlayer**: 2.18.7
- **OkHttp**: 4.9.3
- **Moshi**: 1.11.0
- **Coroutines**: 1.4.3

### Build Configuration

- **Compile SDK**: 34
- **Target SDK**: 35
- **Min SDK**: 21 (Android 5.0+)
- **Version Code**: 2229
- **Version Name**: 3.10.2
- **Application ID**: `com.charles.messenger.v2`

### Adding New Features
1. Create feature package under `presentation/src/main/java/com/charles/messenger/feature/`
2. Create Activity/Controller implementing `QkView<State>`
3. Create Presenter extending `QkPresenter<View, State>`
4. Define State data class
5. Create Dagger module for dependency injection
6. Register module in `ActivityBuilderModule.kt`
7. Add use cases in `domain/src/main/java/com/charles/messenger/interactor/`
8. Implement repository methods in `data/` if needed

### CI/CD
CI/CD builds and tests on every commit:
- Decrypts signing keys and secrets from environment variables
- Builds `withAnalyticsRelease` variant
- Creates both APK and AAB (Android App Bundle)
- Deploys tagged releases to GitHub

Signing configuration is read from environment variables when `CI=true`:
- `keystore_password`
- `key_alias`
- `key_password`

## Translations

Translations are managed through Crowdin. Do not modify string resources directly - contribute via https://crowdin.com/project/qksms
