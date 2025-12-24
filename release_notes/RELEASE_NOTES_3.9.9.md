# Release Notes - Version 3.9.9

## Overview

This release (v3.9.9) includes significant improvements to permissions handling, notification management, UI components, and AI features. The version code has been incremented to 2226 for Google Play Store compatibility.

**Version:** 3.9.9  
**Version Code:** 2226  
**Release Date:** December 24, 2025

---

## Core Changes

### 1. Permission Management Enhancements

#### PermissionManagerImpl (`data/src/main/java/com/charles/messenger/manager/PermissionManagerImpl.kt`)
- **Added foreground service permission support** for Android 14+ (API 34+)
- **Improved notification permission checking** with Android 13+ (Tiramisu) support
  - Uses `POST_NOTIFICATIONS` permission check for Android 13+
  - Falls back to `NotificationManagerCompat.areNotificationsEnabled()` for older versions
- **Enhanced permission validation** across all permission types

#### PermissionManager Interface (`domain/src/main/java/com/charles/messenger/manager/PermissionManager.kt`)
- Added method signature for foreground service permission checking
- Improved interface documentation

### 2. Notification System Improvements

#### NotificationManagerImpl (`presentation/src/main/java/com/charles/messenger/common/util/NotificationManagerImpl.kt`)
- **Enhanced notification channel management**
  - Improved channel creation and configuration
  - Better handling of backup/restore notification channel
  - Added proper channel ID constants (`DEFAULT_CHANNEL_ID`, `BACKUP_RESTORE_CHANNEL_ID`)
- **Improved notification permission checks**
  - Integration with PermissionManager for consistent permission handling
  - Better error handling when notifications are disabled
- **Enhanced notification building**
  - Improved PendingIntent handling with proper flag management for Android M+
  - Better support for group conversations
  - Enhanced notification styling and formatting

### 3. UI/UX Enhancements

#### Base Activity Classes

**QkActivity** (`presentation/src/main/java/com/charles/messenger/common/base/QkActivity.kt`)
- **Added window insets handling** for better system bar compatibility
  - Proper padding for system bars (status bar, navigation bar)
  - Support for edge-to-edge displays
  - Improved content view padding management
- **Enhanced toolbar binding**
  - Better toolbar title management
  - Improved error handling for toolbar initialization
  - Support for activities without toolbars

**QkThemedActivity** (`presentation/src/main/java/com/charles/messenger/common/base/QkThemedActivity.kt`)
- **New base class** for themed activities
- **Improved theming support** with better color management
- **Enhanced window insets handling** specific to themed activities
- Better integration with Material Design components

#### Main Activity (`presentation/src/main/java/com/charles/messenger/feature/main/MainActivity.kt`)
- **Improved permission handling** for foreground services
- **Enhanced activity lifecycle management**
- **Better integration with MainViewModel**
- Updated layout references

#### MainViewModel (`presentation/src/main/java/com/charles/messenger/feature/main/MainViewModel.kt`)
- **Enhanced state management**
- **Improved permission checking logic**
- Better handling of foreground service permissions
- Updated reactive streams for permission updates

#### Compose Activity (`presentation/src/main/java/com/charles/messenger/feature/compose/ComposeActivity.kt`)
- **Significant enhancements** to message composition
- **Improved UI components** and layout handling
- Better integration with ComposeViewModel
- Enhanced error handling

#### ComposeViewModel (`presentation/src/main/java/com/charles/messenger/feature/compose/ComposeViewModel.kt`)
- **Major improvements** to message composition logic
- **Enhanced state management** for compose screen
- Better handling of message sending and validation
- Improved reactive programming patterns

### 4. AI Features

#### Ollama Integration

**OllamaRepositoryImpl** (`data/src/main/java/com/charles/messenger/repository/OllamaRepositoryImpl.kt`)
- **Significant expansion** of Ollama API integration
- **Enhanced error handling** and retry logic
- **Improved request/response handling**
- Better support for various Ollama endpoints
- Enhanced logging and debugging capabilities

**OllamaRepository** (`domain/src/main/java/com/charles/messenger/repository/OllamaRepository.kt`)
- Updated interface to support new features
- Enhanced method signatures

**Ollama Model** (`domain/src/main/java/com/charles/messenger/model/Ollama.kt`)
- **Expanded model definitions** for Ollama API
- **New data classes** for various API responses
- Better type safety and null handling

#### AI Settings

**AiSettingsController** (`presentation/src/main/java/com/charles/messenger/feature/settings/ai/AiSettingsController.kt`)
- **New controller** for AI settings management
- **Comprehensive UI** for configuring AI features
- Better integration with AI settings presenter
- Enhanced user experience for AI configuration

**AiSettingsPresenter** (`presentation/src/main/java/com/charles/messenger/feature/settings/ai/AiSettingsPresenter.kt`)
- **Enhanced presenter logic** for AI settings
- **Improved state management**
- Better handling of AI configuration changes

**AiSettingsState** (`presentation/src/main/java/com/charles/messenger/feature/settings/ai/AiSettingsState.kt`)
- **Expanded state definitions** for AI settings
- Better state management patterns

**AiSettingsView** (`presentation/src/main/java/com/charles/messenger/feature/settings/ai/AiSettingsView.kt`)
- **New view interface** for AI settings
- Better separation of concerns

**AiSettingsController Layout** (`presentation/src/main/res/layout/ai_settings_controller.xml`)
- **New layout file** for AI settings UI
- Comprehensive settings interface

#### AI Auto-Reply

**AiAutoReplyReceiver** (`presentation/src/main/java/com/charles/messenger/receiver/AiAutoReplyReceiver.kt`)
- **Enhanced auto-reply functionality**
- **Improved error handling**
- Better integration with notification system

**GenerateSmartReplies** (`domain/src/main/java/com/charles/messenger/interactor/GenerateSmartReplies.kt`)
- **Improved smart reply generation**
- Better integration with Ollama API

### 5. QkReply Feature

#### QkReplyActivity (`presentation/src/main/java/com/charles/messenger/feature/qkreply/QkReplyActivity.kt`)
- **Enhanced quick reply functionality**
- **Improved UI handling**
- Better integration with notification system

#### QkReplyViewModel (`presentation/src/main/java/com/charles/messenger/feature/qkreply/QkReplyViewModel.kt`)
- **Major improvements** to quick reply logic
- **Enhanced state management**
- Better handling of reply actions
- Improved reactive programming patterns

### 6. Settings Improvements

#### SettingsController (`presentation/src/main/java/com/charles/messenger/feature/settings/SettingsController.kt`)
- **Refactored settings management**
- **Improved code organization**
- Better separation of concerns
- Enhanced maintainability

#### ThemePickerController (`presentation/src/main/java/com/charles/messenger/feature/themepicker/ThemePickerController.kt`)
- **Significant enhancements** to theme picker
- **Improved theme selection UI**
- Better theme preview functionality
- Enhanced user experience

### 7. Compose Screen Enhancements

#### SuggestionChipsAdapter (`presentation/src/main/java/com/charles/messenger/feature/compose/SuggestionChipsAdapter.kt`)
- **Improved suggestion chip handling**
- **Enhanced UI rendering**
- Better integration with compose view model

#### DetailedChipView (`presentation/src/main/java/com/charles/messenger/feature/compose/editing/DetailedChipView.kt`)
- **Enhanced chip view functionality**
- **Improved editing capabilities**

### 8. Repository Improvements

#### MessageRepositoryImpl (`data/src/main/java/com/charles/messenger/repository/MessageRepositoryImpl.kt`)
- **Enhanced message repository** functionality
- **Improved data handling**
- Better error management

#### ReceiveSms (`domain/src/main/java/com/charles/messenger/interactor/ReceiveSms.kt`)
- **Improved SMS receiving logic**
- **Enhanced error handling**

### 9. Utility Improvements

#### DeviceIdManager (`domain/src/main/java/com/charles/messenger/util/DeviceIdManager.kt`)
- **Enhanced device ID management**
- Better device identification

#### Preferences (`domain/src/main/java/com/charles/messenger/util/Preferences.kt`)
- **Expanded preference management**
- **New preference keys** for additional settings
- Better preference handling

### 10. AndroidManifest Updates

#### AndroidManifest.xml (`presentation/src/main/AndroidManifest.xml`)
- **Added foreground service permission declarations**
- **Enhanced permission declarations** for Android 13+
- **Improved service declarations**
- Better compatibility with latest Android versions

### 11. Build Configuration

#### build.gradle (`presentation/build.gradle`)
- **Version updates:**
  - Version Code: 2225 → 2226
  - Version Name: 3.9.8 → 3.9.9
- **Build configuration improvements**

#### ProGuard Rules (`presentation/proguard-rules.pro`)
- **Updated ProGuard rules** for new classes
- **Enhanced obfuscation rules** for AI features
- Better code shrinking support

### 12. Layout Updates

#### Layout Files
- **compose_activity.xml**: Enhanced compose screen layout
- **container_activity.xml**: Improved container layout
- **main_activity.xml**: Updated main activity layout
- **ai_settings_controller.xml**: New AI settings layout

### 13. String Resources

#### strings.xml (`presentation/src/main/res/values/strings.xml`)
- **Added new string resources** for:
  - Foreground service permissions
  - AI settings
  - Enhanced notifications
  - Backup/restore operations

### 14. Documentation

#### New Documentation Files
- **FOREGROUND_SERVICE_DATA_SYNC_DEMONSTRATION.md**: Comprehensive documentation on foreground service permissions
- **test_foreground_service_permission.sh**: Test script for permission handling
- **Video assets**: Demo videos and screenshots for documentation

#### Video Assets
- Created comprehensive demo video scripts
- Added screenshot assets for documentation
- Enhanced visual documentation

### 15. Changelog Updates

#### changelog.json (`data/src/main/assets/changelog.json`)
- **Added version 3.9.9 entry**
- **Updated version 3.9.8 entry**
- Better changelog organization

---

## Technical Details

### Files Changed
- **85 files** modified, added, or updated
- **3,231 insertions**, 161 deletions
- **Major refactoring** in multiple components

### Key Technical Improvements
1. **Better Android version compatibility** (Android 13+, API 33+)
2. **Enhanced permission handling** across the app
3. **Improved notification system** with better channel management
4. **Expanded AI integration** with Ollama
5. **Better UI/UX** with improved base classes
6. **Enhanced error handling** throughout the codebase
7. **Improved code organization** and maintainability

### Architecture Improvements
- Better separation of concerns
- Enhanced MVP pattern implementation
- Improved reactive programming patterns
- Better dependency injection usage
- Enhanced state management

---

## Migration Notes

### For Users
- No special upgrade steps required
- All existing data and settings will be preserved
- App will request new permissions if needed (foreground service, notifications)

### For Developers
- New base classes (`QkThemedActivity`) available for themed activities
- Enhanced permission checking methods in `PermissionManager`
- Updated notification channel management
- New AI settings infrastructure

---

## Known Issues
- None reported at this time

---

## Future Improvements
- Continued AI feature enhancements
- Additional permission handling improvements
- Further UI/UX refinements
- Performance optimizations

---

## Credits

Thank you to all contributors and users who provided feedback and testing for this release.

---

*For detailed technical information, please refer to the commit history and code documentation.*

