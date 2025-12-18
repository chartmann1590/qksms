# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

QKSMS is an open source SMS/MMS messaging application for Android, released under GPLv3. It replaces the stock Android messaging app with enhanced features and customization.

## Build Commands

### Standard Build
```bash
./gradlew assembleDebug                          # Build debug APK
./gradlew assembleRelease                        # Build release APK (requires signing config)
./gradlew :presentation:assembleWithAnalyticsDebug    # Build with analytics (Google Play variant)
./gradlew :presentation:assembleNoAnalyticsDebug      # Build without analytics (F-Droid variant)
```

### Testing
```bash
./gradlew test                                   # Run unit tests
./gradlew connectedAndroidTest                   # Run instrumentation tests (requires device/emulator)
./gradlew :domain:test                           # Run tests for domain module only
./gradlew :data:test                             # Run tests for data module only
```

### Other Commands
```bash
./gradlew androidDependencies                    # Download all Android dependencies
./gradlew clean                                  # Clean all build outputs
./gradlew :presentation:bundleWithAnalyticsRelease    # Build Android App Bundle for Play Store
```

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

- **withAnalytics**: Includes Firebase Crashlytics, Analytics, Performance, Google Play Billing, and Amplitude
- **noAnalytics**: Clean build without tracking (for F-Droid)

Flavor configuration is in `presentation/build.gradle` and `data/build.gradle`.

### Feature Module Organization

Each feature in `presentation/src/main/java/com/charles/messenger/feature/` is organized as:
- Activity/Controller - View implementation
- Presenter - State management and business logic orchestration
- ViewModel/State data classes
- Dagger injection modules (e.g., `ComposeActivityModule.kt`)

Major features:
- `compose/` - Message composition screen
- `conversations/` - Conversation list
- `main/` - Main app container
- `blocking/` - Number blocking management
- `settings/` - App settings and preferences
- `plus/` - Premium features and billing

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
CircleCI builds and tests on every commit:
- Decrypts signing keys and secrets
- Builds `WithAnalyticsRelease` variant
- Creates both APK and AAB (Android App Bundle)
- Deploys tagged releases to GitHub

## Translations

Translations are managed through Crowdin. Do not modify string resources directly - contribute via https://crowdin.com/project/qksms
