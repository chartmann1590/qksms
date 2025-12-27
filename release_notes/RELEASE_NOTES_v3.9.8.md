# Release v3.9.8

## What's New

This release focuses on improving permissions handling, notification management, and UI components for better user experience and Android compatibility.

### Improvements

- **Enhanced Permission Management**
  - Added support for foreground service permissions
  - Improved permission checking for Android 13+ (Tiramisu)
  - Better handling of notification permissions across Android versions

- **Notification System Updates**
  - Improved notification channel management
  - Enhanced backup/restore notification handling
  - Better notification permission checks

- **UI/UX Enhancements**
  - Updated activity layouts and base classes
  - Improved window insets handling for better system bar compatibility
  - Enhanced toolbar binding and title management
  - Updated MainActivity, ComposeActivity, and related UI components

- **Code Quality**
  - Code cleanup and improvements
  - Better error handling
  - Updated documentation

### Technical Changes

- Updated `PermissionManagerImpl` with foreground service permission support
- Enhanced `NotificationManagerImpl` with improved channel management
- Added `QkThemedActivity` base class for better theming support
- Improved `QkActivity` with better window insets handling
- Updated AndroidManifest with new permission declarations
- Various bug fixes and stability improvements

### Documentation

- Added comprehensive foreground service documentation
- Updated test scripts and demo materials
- Enhanced changelog and release notes

---

**Version:** 3.9.8  
**Version Code:** 2225  
**Build Date:** 2025-12-23

---

## Installation

Download the APK from the [Releases page](https://github.com/chartmann1590/textpilot/releases) or install from Google Play Store.

## Upgrade Notes

- No special upgrade steps required
- All existing data and settings will be preserved
- If you experience any issues, please report them on GitHub

---

*Thank you for using QKSMS! If you enjoy the app, please consider upgrading to Messenger+ to unlock premium features and support development.*

