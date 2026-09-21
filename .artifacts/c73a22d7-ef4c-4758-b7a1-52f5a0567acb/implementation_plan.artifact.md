# Fix Unresolved Reference 'compose' Error

The project currently contains Jetpack Compose code in `MainActivity.kt`, but the build configuration (`app/build.gradle.kts` and `libs.versions.toml`) does not include the necessary Compose dependencies or the Compose Compiler configuration.

## Proposed Changes

### [Component Name] Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/CodeAlpha/Task-1/gradle/libs.versions.toml)
- Add versions for Kotlin and Compose BOM.
- Add Compose-related library definitions.
- Add Compose Compiler plugin definition.
- Fix incorrect `androidx-activity-ktx` definition.

#### [MODIFY] [build.gradle.kts](file:///D:/CodeAlpha/Task-1/build.gradle.kts) (Top-level)
- Add Kotlin Android and Compose Compiler plugins to the `plugins` block (applied `false`).

#### [MODIFY] [app/build.gradle.kts](file:///D:/CodeAlpha/Task-1/app/build.gradle.kts)
- Apply Kotlin Android and Compose Compiler plugins.
- Enable Compose in `buildFeatures`.
- Add Jetpack Compose dependencies using the BOM.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully.
- Run `gradle_sync` to ensure the IDE recognizes the new dependencies.

### Manual Verification
- Verify that `MainActivity.kt` no longer shows unresolved reference errors for `androidx.compose`.
