# Build notes

- compileSdk 35 / targetSdk 35
- minSdk 26
- AGP 8.7.3
- Gradle 8.11.1
- Java 17 on Codemagic
- Package: com.autofolder.organizer
- Normal app only; no CATEGORY_HOME intent filter
- AccessibilityService is user-enabled and started only by an explicit run
- No uninstall API calls are present
