# AutoFolder v0.2 patch

This build fixes two problems found in the first field test on iQOO Z7 Pro / Funtouch OS 15:

1. **Organization did not start after leaving the app.** The accessibility service was already connected before the Start button was pressed, so the previous build only checked the `running` flag during `onServiceConnected()`. The patched service starts when it sees the existing iQOO home launcher become visible after Start.
2. **App details were hidden.** The Scan screen now lists each detected app under its category instead of showing only category counts.
3. **Drag gesture was not a true long-press.** The patched gesture holds the source icon for 650 ms before moving it, which better matches the iQOO launcher drag workflow.

This remains a normal app. It does not declare itself as a HOME/launcher app and does not uninstall packages.
