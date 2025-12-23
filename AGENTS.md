# Repository Guidelines

## Project Structure & Module Organization
- `presentation/` — Android app module (UI, DI, activities/controllers, resources). Flavors: `withAnalytics`, `noAnalytics`.
  - Feature-based organization under `com.charles.messenger.feature`
  - Uses MVP pattern with unidirectional state flow
  - Navigation via Conductor framework
- `domain/` — business logic, use cases, interfaces (Kotlin, no Android UI).
  - Use cases extend `Interactor<Params>`
  - Repository interfaces
  - Domain models and business rules
- `data/` — repositories, data sources, networking, persistence.
  - Repository implementations
  - Realm database and ContentProvider access
  - Product flavors: `withAnalytics` (includes Amplitude), `noAnalytics`
- `android-smsmms/` — telephony/MMS utilities used by other modules.
  - Forked from klinker41/android-smsmms
  - Handles low-level SMS/MMS protocol operations
- `common/` — shared utilities, extensions, and Android helpers.
  - Extension functions and null-safety helpers
  - Common models and utilities
  - DebugLogger utility for runtime debugging
- Tests live mainly in `*/src/androidTest/java/...` (Kotlin `*Test.kt`).

## Build, Test, and Development Commands
- Build debug APK: `./gradlew :presentation:assembleNoAnalyticsDebug` (recommended for development)
- Build with analytics: `./gradlew :presentation:assembleWithAnalyticsDebug`
- Install on device: `./gradlew :presentation:installNoAnalyticsDebug`
- Lint (all modules): `./gradlew lint`
- Unit tests (where present): `./gradlew test`
- Instrumented tests: `./gradlew :presentation:connectedNoAnalyticsDebugAndroidTest` (emulator/device required)
- Release artifact (signed, analytics on): `./gradlew :presentation:assembleWithAnalyticsRelease`
- Build Android App Bundle: `./gradlew :presentation:bundleWithAnalyticsRelease`
- Clean build: `./gradlew clean`
  - On Windows use `gradlew.bat ...`.

## Coding Style & Naming Conventions
- Language: Kotlin, 4-space indentation, UTF-8, no wildcard imports.
- Names: Classes/Objects `UpperCamelCase`; functions/vars `lowerCamelCase`; constants `UPPER_SNAKE_CASE`.
- Resources: lower_snake_case (e.g., `activity_main.xml`, `ic_message_send.xml`, string keys like `main_title`).
- Prefer extension functions and null-safety helpers under `common/` (see `com.charles.messenger.common.util.extensions`).

## Testing Guidelines
- Instrumented tests under `src/androidTest/java/...`, name files `*Test.kt`.
- Frameworks: AndroidX Test, Espresso, Mockito; JUnit 4 for unit tests.
- Provide a clear test plan in PRs (variants tested, device/emulator, steps to verify).

## Commit & Pull Request Guidelines
- Commits: imperative present (“Fix crash…”, “Add support…”), concise scope; reference module when useful (e.g., `presentation:`).
- Link issues (`#123`) and include context for behavior changes.
- PRs must include: summary, before/after or screenshots for UI, reproduction/verification steps, and any migration notes.

## Security & Configuration Tips
- Do not commit secrets/keystores. Use `local.properties` for signing and AdMob:
  - `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `ADMOB_*`.
- Provide analytics keys via environment vars where used (e.g., `AMPLITUDE_API_KEY`).
- Choose flavors consciously: `noAnalytics` for development; `withAnalytics` for release/testing external services.

## Important Development Notes
- **Realm Plugin Ordering:** Always apply Realm plugin BEFORE Kotlin plugin in `build.gradle`:
  ```gradle
  apply plugin: 'realm-android'  // Must be first
  apply plugin: 'kotlin-android'
  ```
- **Architecture Pattern:** MVP with unidirectional state flow
  - Views implement `QkView<State>` with `render(state: State)` method
  - Presenters extend `QkPresenter<View, State>`
  - State managed via RxJava BehaviorSubject
- **Dependency Injection:** Dagger 2 with scopes (`@ActivityScope`, `@ControllerScope`, `@ServiceScope`)
- **Reactive Programming:** Heavy use of RxJava 2 for async operations
- **Database:** Realm with reactive queries (Flowable support)

