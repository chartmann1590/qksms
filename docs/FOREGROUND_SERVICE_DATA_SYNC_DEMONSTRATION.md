# FOREGROUND_SERVICE_DATA_SYNC Permission Demonstration Guide

## Overview

This document explains how to demonstrate the `FOREGROUND_SERVICE_DATA_SYNC` permission in action. This permission is required for Android 14+ (API 34+) when using a foreground service with `foregroundServiceType="dataSync"`.

## What This Permission Does

The `FOREGROUND_SERVICE_DATA_SYNC` permission allows the app to run a foreground service that synchronizes data (in this case, restoring backup data). On Android 14+, foreground services must declare a specific type, and the corresponding permission must be granted.

## Video Demonstration Script

### Scene 1: Introduction (0:00 - 0:15)
**What to show:**
- Open the QKSMS app
- Navigate to Settings → Backup & Restore
- Explain: "We're going to demonstrate the FOREGROUND_SERVICE_DATA_SYNC permission by restoring a backup"

**Narration:**
> "The FOREGROUND_SERVICE_DATA_SYNC permission is required on Android 14+ for data synchronization operations. In QKSMS, this is used when restoring messages from a backup file."

### Scene 2: Check Android Version (0:15 - 0:30)
**What to show:**
- Open device Settings → About Phone
- Show Android version (must be 14+ for permission to be required)
- Return to app

**Narration:**
> "This permission is specifically required on Android 14 and above. On older versions, the general FOREGROUND_SERVICE permission is sufficient."

### Scene 3: Verify Permission in Manifest (0:30 - 0:45)
**What to show:**
- Open AndroidManifest.xml in code editor
- Highlight the permission declaration:
  ```xml
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
  ```
- Show the service declaration:
  ```xml
  <service android:name="com.charles.messenger.feature.backup.RestoreBackupService"
      android:foregroundServiceType="dataSync" />
  ```

**Narration:**
> "The permission is declared in the manifest, and the service is configured with foregroundServiceType='dataSync'. This tells Android that the service performs data synchronization."

### Scene 4: Start Backup Restore (0:45 - 1:15)
**What to show:**
- In the app, tap "Restore" button
- Select a backup file from storage
- Confirm the restore operation
- Show the notification that appears immediately

**Narration:**
> "When we start a restore operation, the RestoreBackupService is launched as a foreground service. Notice the notification appears immediately - this is required for foreground services."

### Scene 5: Show Service Running (1:15 - 1:45)
**What to show:**
- Pull down notification shade
- Show the restore notification with progress
- Open Settings → Apps → QKSMS → App Info
- Show "Running services" or "Active services"
- Show RestoreBackupService listed as active

**Narration:**
> "The service runs in the foreground, showing a persistent notification. This ensures the restore operation continues even if the user switches apps. The FOREGROUND_SERVICE_DATA_SYNC permission allows this data sync operation to run."

### Scene 6: Demonstrate Without Permission (1:45 - 2:30)
**What to show:**
- Use ADB to revoke the permission:
  ```bash
  adb shell pm revoke com.charles.messenger.v2 android.permission.FOREGROUND_SERVICE_DATA_SYNC
  ```
- Try to start restore again
- Show error or service failing to start
- Re-grant permission:
  ```bash
  adb shell pm grant com.charles.messenger.v2 android.permission.FOREGROUND_SERVICE_DATA_SYNC
  ```
- Show restore working again

**Narration:**
> "Without the permission, the service cannot start on Android 14+. This demonstrates why the permission is essential for the backup restore feature to work."

### Scene 7: Show Code Implementation (2:30 - 3:00)
**What to show:**
- Open RestoreBackupService.kt
- Highlight the `startForeground()` call
- Show how the service uses `ContextCompat.startForegroundService()`
- Explain the data sync operation

**Narration:**
> "The service calls startForeground() to become a foreground service. The dataSync type indicates this service synchronizes data - in our case, restoring messages from a backup file to the local database."

### Scene 8: Completion (3:00 - 3:15)
**What to show:**
- Show restore completing successfully
- Notification disappears
- Messages appear in the app

**Narration:**
> "The FOREGROUND_SERVICE_DATA_SYNC permission ensures backup restore works reliably on Android 14+, allowing users to restore their messages even when the app is in the background."

## Key Points to Highlight

1. **Android 14+ Requirement**: The permission is only required on Android 14 (API 34) and above
2. **Service Type**: The service must be declared with `foregroundServiceType="dataSync"`
3. **User Experience**: The permission is automatically granted - users don't need to approve it
4. **Functionality**: Without this permission, backup restore would fail on Android 14+
5. **Notification**: The service shows a persistent notification, which is required for foreground services

## Testing Instructions

### Prerequisites
- Android 14+ device or emulator
- QKSMS app installed
- A backup file to restore

### Steps to Test

1. **Verify Permission is Declared:**
   ```bash
   adb shell dumpsys package com.charles.messenger.v2 | grep FOREGROUND_SERVICE_DATA_SYNC
   ```

2. **Check Service Declaration:**
   ```bash
   adb shell dumpsys package com.charles.messenger.v2 | grep -A 5 RestoreBackupService
   ```

3. **Start Restore and Monitor:**
   ```bash
   # Start restore in app, then check running services
   adb shell dumpsys activity services | grep RestoreBackupService
   ```

4. **Test Without Permission (Android 14+):**
   ```bash
   # Revoke permission
   adb shell pm revoke com.charles.messenger.v2 android.permission.FOREGROUND_SERVICE_DATA_SYNC
   
   # Try to start restore - should fail
   # Check logcat for errors
   adb logcat | grep -i "foreground.*service"
   
   # Re-grant permission
   adb shell pm grant com.charles.messenger.v2 android.permission.FOREGROUND_SERVICE_DATA_SYNC
   ```

## Logcat Output to Look For

When the service starts successfully:
```
I/ActivityManager: Start service com.charles.messenger.v2/.feature.backup.RestoreBackupService
I/RestoreBackupService: Starting restore operation
```

If permission is missing (Android 14+):
```
E/ActivityManager: Permission denial: starting Intent { ... } from ... requires android.permission.FOREGROUND_SERVICE_DATA_SYNC
```

## Code References

- **Service Declaration**: `presentation/src/main/AndroidManifest.xml` (line 223-225)
- **Permission Declaration**: `presentation/src/main/AndroidManifest.xml` (line 28)
- **Service Implementation**: `presentation/src/main/java/com/charles/messenger/feature/backup/RestoreBackupService.kt`
- **Service Invocation**: `presentation/src/main/java/com/charles/messenger/feature/backup/BackupPresenter.kt` (line 113)

## Summary

The `FOREGROUND_SERVICE_DATA_SYNC` permission is essential for the backup restore feature on Android 14+. It allows the `RestoreBackupService` to run as a foreground service, ensuring restore operations complete reliably even when the app is backgrounded. The permission is automatically granted and doesn't require user approval, but it must be declared in the manifest for the service to function on Android 14+.


