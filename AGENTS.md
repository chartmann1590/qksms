# Repository Guidelines

## Project Structure & Module Organization
- `presentation/` — Android app module (UI, DI, activities/controllers, resources). Flavors: `withAnalytics`, `noAnalytics`.
- `domain/` — business logic, use cases, interfaces (Kotlin, no Android UI).
- `data/` — repositories, data sources, networking, persistence.
- `android-smsmms/` — telephony/MMS utilities used by other modules.
- `common/` — shared utilities, extensions, and Android helpers.
- Tests live mainly in `*/src/androidTest/java/...` (Kotlin `*Test.kt`).

## Build, Test, and Development Commands
- Build debug APK: `./gradlew :presentation:assembleNoAnalyticsDebug`
- Install on device: `./gradlew :presentation:installNoAnalyticsDebug`
- Lint (all modules): `./gradlew lint`
- Unit tests (where present): `./gradlew test`
- Instrumented tests: `./gradlew :presentation:connectedNoAnalyticsDebugAndroidTest` (emulator/device required)
- Release artifact (signed, analytics on): `./gradlew :presentation:assembleWithAnalyticsRelease`
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

