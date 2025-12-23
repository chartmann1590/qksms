# Privacy Policy for QKSMS

**Last Updated:** January 2025

## Introduction

QKSMS ("we", "our", or "the app") is an open source SMS and MMS messaging application for Android. We are committed to protecting your privacy. This Privacy Policy explains how QKSMS handles your data.

## Data Collection

### What We Do NOT Collect

QKSMS does **not** collect, transmit, or share any of the following:
- Your text messages (SMS/MMS)
- Your contacts
- Your phone number
- Your location
- Any personal information
- Usage analytics (in the NoAnalytics variant)

### Local Data Storage

All your data remains on your device:
- **Messages:** Stored locally on your device using Android's SMS database and Realm database
- **Contacts:** Accessed from your device's contact list but never transmitted
- **Settings & Preferences:** Stored locally on your device
- **Backups:** Created and stored locally on your device (if you use the backup feature)
- **Trial Usage:** A hashed device identifier is stored locally to manage the Messenger+ 7-day free trial across app reinstalls (never transmitted to external servers)

### Analytics Variants

QKSMS is available in two variants:

1. **NoAnalytics Variant (F-Droid):** No analytics, tracking, or third-party services. Complete privacy.

2. **WithAnalytics Variant (Google Play):** May include:
   - Firebase Analytics for app usage statistics
   - Firebase Crashlytics for crash reporting
   - Firebase Performance Monitoring
   - Amplitude for analytics (in data module)
   - Google Play Billing for in-app purchases

If you use the WithAnalytics variant, Google's privacy policies apply to the data collected by these services. You can review Google's privacy policy at: https://policies.google.com/privacy

**Note:** The NoAnalytics variant contains zero tracking or analytics services and is recommended for privacy-focused users.

## Permissions

QKSMS requires the following permissions to function:

- **SMS/MMS:** To send, receive, and read your messages
- **Contacts:** To display contact names and photos in conversations
- **Phone:** For dual SIM support and call functionality
- **Storage:** For saving media attachments and creating backups
- **Notifications:** To alert you of new messages
- **Internet:** For MMS messaging and optional AI Smart Reply feature

## AI Smart Reply Feature

If you enable the AI Smart Reply or Auto-Reply features:
- Your messages are sent **only** to your own locally-hosted Ollama server
- The Ollama server runs on your own hardware (e.g., your computer or local network)
- **No data is sent to external AI services or cloud providers**
- You configure the Ollama API URL yourself (typically your local network IP)
- You control where your data goes - it never leaves your network
- The app connects directly to your Ollama instance via HTTP
- No third-party AI services (OpenAI, Google, etc.) are involved

## Third-Party Services

QKSMS may integrate with:
- **Your mobile carrier:** For sending SMS/MMS (standard carrier policies apply)
- **Optional blocking apps:** Call Blocker, Call Control, Should I Answer (their privacy policies apply)
- **Your Ollama server:** If you enable AI Smart Reply (self-hosted, you control the data)
- **Google Play Services:** Only in the WithAnalytics variant for Firebase services and billing
- **F-Droid:** For distribution of the NoAnalytics variant (no tracking)

**Note:** All integrations are optional except for carrier services required for SMS/MMS functionality.

## Data Security

- All message data is stored locally on your device
- We do not operate servers that store your data
- Your device's security measures protect your messages

## Children's Privacy

QKSMS does not knowingly collect any personal information from children under 13.

## Changes to This Policy

We may update this Privacy Policy from time to time. Changes will be posted in this file with an updated "Last Updated" date.

## Open Source

QKSMS is open source software licensed under GPLv3. You can review the complete source code at:
https://github.com/chartmann1590/qksms

## Contact

If you have questions about this Privacy Policy, please contact:

- **Developer:** Charles Hartmann
- **Email:** tickets@portfolio-8ul8id.p.tawk.email
- **Source Code:** https://github.com/chartmann1590/qksms

## Summary

**Your privacy is protected.** QKSMS stores all your data locally on your device. We do not collect, transmit, or sell your personal information. The NoAnalytics variant contains zero tracking or analytics. You are in complete control of your data.

**Key Privacy Points:**
- All messages stored locally on your device
- No cloud sync or external servers
- NoAnalytics variant has zero tracking
- AI Smart Reply uses your own local Ollama server
- Optional local backups stored on your device
- Open source code - you can verify everything yourself

