# AutoFolder — iQOO/Funtouch OS 15 prototype

Native Android app for iQOO Z7 Pro 5G / Android 15 / Funtouch OS 15.

## What it does
- Remains a normal app; it is NOT a launcher and does not declare itself as a Home app.
- Scans launchable installed apps and groups them with local rules.
- Uses a user-enabled AccessibilityService only after the user presses Start.
- Opens the stock launcher/app drawer and performs deterministic long-press/drag operations to create folders and add apps.
- Never calls uninstall APIs.

## Important
The stock launcher owns the home-screen database. Android does not expose a universal API for another app to edit a launcher's folders. This prototype therefore operates the visible launcher UI like a user would. Funtouch/iQOO launcher updates can change coordinates or accessibility nodes, so test with a small batch first.

For safety, keep one or more empty home-screen cells available. If the launcher rejects a drag or an app cannot be found, the service stops rather than continuing blindly.
