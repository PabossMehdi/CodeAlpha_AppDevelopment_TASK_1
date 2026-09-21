# Fix Unresolved Reference 'compose' by Enabling Jetpack Compose

The project's `MainActivity.kt` uses Jetpack Compose, but the build configuration (`build.gradle.kts` and `libs.versions.toml`) is missing the necessary dependencies and feature flags. This plan will add the required Compose libraries and enable the Compose compiler.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/CodeAlpha/Task-1/gradle/libs.versions.toml)
- Add versions for Kotlin, Compose BOM, and Compose Material3.
- Add Compose-related libraries: `activity-compose`, `compose-bom`, `ui`, `ui-graphics`, `ui-tooling-preview`, `material3`, `foundation`, `runtime`.
- Add plugin definitions for `kotlin-android` and `kotlin-compose`.

#### [MODIFY] [build.gradle.kts](file:///D:/CodeAlpha/Task-1/build.gradle.kts)
- Add `kotlin-android` and `kotlin-compose` plugins to the top-level build file.

#### [MODIFY] [app/build.gradle.kts](file:///D:/CodeAlpha/Task-1/app/build.gradle.kts)
- Apply `kotlin-android` and `kotlin-compose` plugins.
- Enable `compose` in `buildFeatures`.
- Add Compose dependencies using the BOM.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference error is resolved.
- Run a full build: `./gradlew assembleDebug`.

### Manual Verification
- Verify that Android Studio no longer shows red errors in `MainActivity.kt`.
