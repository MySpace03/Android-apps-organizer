# AutoFolder — Cloud Build Package

Target: iQOO Z7 Pro 5G / Android 15 / Funtouch OS 15.

This is a normal Android app. It does **not** declare itself as a launcher and
never calls uninstall APIs. The app uses an explicitly user-enabled
AccessibilityService to automate the existing iQOO launcher UI.

## Build online with Codemagic

1. Create a GitHub repository and upload the contents of this folder to the
   repository root. In particular, `codemagic.yaml`, `settings.gradle`,
   `build.gradle`, `app/`, `gradlew`, and `gradle/wrapper/` must be at the root.
2. Sign in to Codemagic and add the GitHub repository as an Android native app.
3. In the build configuration, choose the `android-debug-apk` workflow first.
4. Start a build.
5. Download `app-debug.apk` from the build's Artifacts section.
6. Install the APK on the iQOO phone.

The `android-release-unsigned-apk` workflow is included for producing a release
APK for sideloading without configuring a signing keystore. For a signed
release, add a keystore in Codemagic and wire `CM_KEYSTORE_PATH`,
`CM_KEYSTORE_PASSWORD`, `CM_KEY_ALIAS`, and `CM_KEY_PASSWORD` into a release
signing config.

## Wrapper note

The repository includes `gradlew`, `gradlew.bat`, and
`gradle/wrapper/gradle-wrapper.properties` pinned to Gradle 8.11.1.

The standard binary `gradle-wrapper.jar` is not vendored in this sandbox copy
because the build environment cannot fetch it. The Codemagic workflow downloads
the official Gradle 8.11.1 wrapper JAR at build time, verifies its SHA-256 hash,
and then runs the normal Gradle wrapper. The included `gradlew` also falls back
to the preinstalled `gradle` command or bootstraps the pinned distribution when
used outside Codemagic.

## First-run test recommendation

Use the app's Test/limited organization flow first. Keep the phone unlocked and
do not touch the screen while an automation run is active. Funtouch OS launcher
UI can vary between builds, so the accessibility automation should be treated
as a device-specific prototype until tested on the physical phone.
