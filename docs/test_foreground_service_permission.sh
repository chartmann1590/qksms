#!/bin/bash
# Test script to verify FOREGROUND_SERVICE_DATA_SYNC permission
# Usage: ./test_foreground_service_permission.sh

PACKAGE_NAME="com.charles.messenger.v2"
PERMISSION="android.permission.FOREGROUND_SERVICE_DATA_SYNC"
SERVICE_NAME="com.charles.messenger.feature.backup.RestoreBackupService"

echo "=========================================="
echo "FOREGROUND_SERVICE_DATA_SYNC Permission Test"
echo "=========================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected"
    echo "Please connect a device or start an emulator"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Check Android version
ANDROID_VERSION=$(adb shell getprop ro.build.version.sdk)
echo "Android SDK Version: $ANDROID_VERSION"

if [ "$ANDROID_VERSION" -lt 34 ]; then
    echo "⚠️  Warning: This permission is only required on Android 14+ (API 34+)"
    echo "   Current version: API $ANDROID_VERSION"
    echo "   The permission will be ignored on this version"
    echo ""
fi

# Check if app is installed
if ! adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "❌ App not installed: $PACKAGE_NAME"
    exit 1
fi

echo "✅ App installed: $PACKAGE_NAME"
echo ""

# Check if permission is declared in manifest
echo "Checking if permission is declared in manifest..."
if adb shell dumpsys package $PACKAGE_NAME | grep -q "$PERMISSION"; then
    echo "✅ Permission declared in manifest: $PERMISSION"
else
    echo "❌ Permission NOT found in manifest: $PERMISSION"
    exit 1
fi
echo ""

# Check if service is declared with correct type
echo "Checking service declaration..."
if adb shell dumpsys package $PACKAGE_NAME | grep -A 5 "RestoreBackupService" | grep -q "dataSync"; then
    echo "✅ Service declared with foregroundServiceType='dataSync'"
else
    echo "⚠️  Service type not found or incorrect"
fi
echo ""

# Check current permission status
echo "Checking permission status..."
PERMISSION_STATUS=$(adb shell dumpsys package $PACKAGE_NAME | grep -A 1 "$PERMISSION" | tail -1)

if echo "$PERMISSION_STATUS" | grep -q "granted=true"; then
    echo "✅ Permission granted: $PERMISSION"
elif echo "$PERMISSION_STATUS" | grep -q "granted=false"; then
    echo "⚠️  Permission not granted: $PERMISSION"
    echo "   This is normal - the permission is auto-granted when needed"
else
    echo "ℹ️  Permission status: $PERMISSION_STATUS"
fi
echo ""

# Test permission grant/revoke (Android 14+ only)
if [ "$ANDROID_VERSION" -ge 34 ]; then
    echo "Testing permission grant/revoke (Android 14+)..."
    echo ""
    
    # Grant permission
    echo "Granting permission..."
    adb shell pm grant $PACKAGE_NAME $PERMISSION
    if [ $? -eq 0 ]; then
        echo "✅ Permission granted successfully"
    else
        echo "❌ Failed to grant permission"
    fi
    echo ""
    
    # Check if service can start (this would require actually starting the service)
    echo "To test service startup:"
    echo "1. Open the app"
    echo "2. Go to Backup & Restore"
    echo "3. Start a restore operation"
    echo "4. Check logcat for any permission errors:"
    echo "   adb logcat | grep -i 'foreground.*service'"
    echo ""
    
    # Revoke permission to test failure
    read -p "Revoke permission to test failure scenario? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Revoking permission..."
        adb shell pm revoke $PACKAGE_NAME $PERMISSION
        echo "⚠️  Permission revoked"
        echo "   Try starting a restore - it should fail"
        echo "   Re-grant with: adb shell pm grant $PACKAGE_NAME $PERMISSION"
    fi
else
    echo "ℹ️  Permission grant/revoke test skipped (Android 14+ required)"
fi

echo ""
echo "=========================================="
echo "Test Complete"
echo "=========================================="


