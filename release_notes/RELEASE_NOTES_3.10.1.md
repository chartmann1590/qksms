# Release Notes - Version 3.10.1

## Overview

This release (v3.10.1) includes rebranding updates to the web interface, comprehensive documentation for the Web SMS feature, and version updates for app store compatibility.

**Version:** 3.10.1  
**Version Code:** 2228  
**Release Date:** December 2024

---

## Core Changes

### 1. Web Interface Rebranding

#### TextPilot Branding Updates
- **Changed all web interface titles** from "QKSMS" to "TextPilot" for brand consistency
- **Updated HTML titles** in web client and placeholder pages
- **Updated login interface** to display "TextPilot Web" branding
- **Updated package names** in `package.json` files:
  - `qksms-web-client` → `textpilot-web-client`
  - `qksms-web-server` → `textpilot-web-server`
- **Updated localStorage keys** from `qksms_device_id` to `textpilot_device_id` for consistency

#### Files Updated
- `web-interface/client/index.html` - Updated page title
- `web-interface/client-placeholder.html` - Updated title and heading
- `web-interface/client/src/components/Login/Login.tsx` - Updated branding text
- `web-interface/client/src/services/api.ts` - Updated device ID storage key
- `web-interface/client/package.json` - Updated package name and description
- `web-interface/server/package.json` - Updated package name and description
- `web-interface/README.md` - Updated all references to TextPilot
- `web-interface/client/README.md` - Updated all references to TextPilot

### 2. Web SMS Feature Documentation

#### New Documentation File
- **Created comprehensive Web SMS feature documentation** at `docs/WEB_SMS_FEATURE.md`
- **Detailed architecture explanation** with diagrams
- **Complete setup instructions** for server and Android app
- **Technical details** on sync mechanism, security, and components
- **Troubleshooting guide** for common issues
- **Development status** and future enhancements

#### Documentation Highlights
- **Architecture Overview**: Client-server architecture with PostgreSQL database
- **Data Flow**: Initial sync, incremental sync, and real-time updates
- **Security Features**: JWT authentication, encrypted credentials, rate limiting
- **Component Details**: Backend server, web client, and Android integration
- **Setup Instructions**: Step-by-step guide for deployment
- **Technical Details**: Database schema, sync mechanism, WebSocket events

### 3. Version Updates

#### Build Configuration
- **Version Code**: Updated from 2227 to 2228 for app store compatibility
- **Version Name**: Updated from 3.10.0 to 3.10.1

#### Files Updated
- `presentation/build.gradle` - Updated version code and version name

### 4. Documentation Updates

#### Updated Documentation
- **Web Interface README**: Updated all references from QKSMS to TextPilot
- **Web Client README**: Updated branding and references
- **Main README**: Already contained TextPilot branding (no changes needed)

---

## Web SMS Feature Highlights

### What is Web SMS?

The Web SMS feature allows you to access and manage your SMS/MMS messages from any web browser. This self-hosted solution provides a secure, real-time interface that syncs with your Android device.

### Key Features

- 🔒 **Self-hosted**: Complete control over your messaging data
- 🔐 **Secure**: End-to-end authentication with JWT tokens and encrypted credentials
- ⚡ **Real-time**: WebSocket-based instant message updates
- 📱 **Two-way sync**: Send and receive messages from both phone and web
- 🖼️ **MMS Support**: View and send multimedia messages with attachments
- 🐳 **Easy deployment**: One-command Docker setup

### How It Works

1. **Initial Sync**: When you first enable Web Sync, all conversations and messages are uploaded to your server
2. **Incremental Sync**: New messages are automatically synced to the server
3. **Real-time Updates**: The web client receives instant updates via WebSocket
4. **Two-Way Communication**: Send messages from web, receive on phone and vice versa

### Current Status

- ✅ Backend Server - Fully functional REST API
- ✅ Database - PostgreSQL for message storage
- ✅ Real-time Sync - WebSocket support
- ✅ Android Integration - Full sync support
- ✅ Web Client - React-based interface (in development)
- ✅ Docker Deployment - One-command setup
- ✅ Security - JWT authentication, encryption, rate limiting

For detailed information, see `docs/WEB_SMS_FEATURE.md`.

---

## Technical Details

### Files Changed
- **15 files** modified or created
- **Major updates** to web interface branding
- **New documentation** for Web SMS feature
- **Version updates** for app store

### Key Technical Improvements
1. **Brand consistency** across web interface
2. **Comprehensive documentation** for Web SMS feature
3. **Version updates** for app store compatibility
4. **Improved user experience** with consistent branding

### Architecture Improvements
- Better brand consistency across all components
- Enhanced documentation for developers and users
- Improved onboarding experience for Web SMS feature

---

## Migration Notes

### For Users
- No special upgrade steps required
- All existing data and settings will be preserved
- Web interface will now display "TextPilot" branding
- Existing web sync configurations will continue to work

### For Developers
- Web interface package names have changed
- Device ID storage key has changed (will auto-migrate)
- New documentation available in `docs/WEB_SMS_FEATURE.md`
- Updated version code and name in build configuration

---

## Known Issues
- None reported at this time

---

## Future Improvements
- Continued Web SMS feature development
- Enhanced web client UI
- Additional documentation updates
- Performance optimizations

---

## Credits

Thank you to all contributors and users who provided feedback and testing for this release.

---

*For detailed technical information, please refer to the commit history and code documentation.*

