# TextPilot Web SMS Feature

## Overview

The TextPilot Web SMS feature allows you to access and manage your SMS/MMS messages from any web browser. This self-hosted solution provides a secure, real-time interface that syncs with your Android device, enabling you to send and receive messages from your computer or any device with a web browser.

## Status: In Development

The Web SMS feature is currently in active development. The backend infrastructure is complete and functional, with the web client interface being continuously improved.

### Current Implementation Status

- ✅ **Backend Server** - Fully functional REST API with authentication (Node.js/Express/TypeScript)
- ✅ **Database** - PostgreSQL database with TypeORM for message storage
- ✅ **Real-time Sync** - WebSocket support (Socket.io) for instant updates
- ✅ **Android Integration** - Complete sync support with background JobService
- ✅ **Web Client** - React/TypeScript interface with Redux state management
- ✅ **Docker Deployment** - One-command setup with Docker Compose
- ✅ **Security** - JWT authentication, bcrypt password hashing, rate limiting, CORS protection
- ✅ **MMS Support** - Full attachment handling and media support
- ✅ **Background Service** - Automatic periodic sync every 1 minute
- ✅ **Instant Sync** - Triggers immediately when messages are sent/received
- ✅ **Queue System** - Web-sent messages queued and sent by Android app

## How It Works

### Architecture

The Web SMS feature uses a client-server architecture with the following components:

```
┌─────────────────┐         ┌──────────────────┐         ┌──────────────┐
│  TextPilot      │◄──────►│  Web Server      │◄──────►│  PostgreSQL  │
│  Android App    │  HTTPS │  (Node.js/Express)│         │  Database    │
└─────────────────┘         └──────────────────┘         └──────────────┘
                                      │
                                      │ WebSocket
                                      ▼
                             ┌──────────────────┐
                             │  Web Client      │
                             │  (React/TS)      │
                             └──────────────────┘
```

### Data Flow

1. **Initial Sync**: When you first enable Web Sync in the TextPilot Android app:
   - The app authenticates with the web server using your credentials
   - All conversations and messages are uploaded to the server in batches
   - The server stores everything in PostgreSQL
   - A sync token is generated and stored for future incremental syncs

2. **Incremental Sync**: After the initial sync:
   - New messages received on your phone trigger instant sync via WebSyncService
   - Messages sent from your phone also trigger instant sync
   - Background service runs every 1 minute for periodic backup sync
   - The app periodically checks for messages queued from the web interface
   - Only new/updated messages are transmitted, making syncs fast and efficient

3. **Real-time Updates**: 
   - The web client connects via WebSocket to receive instant updates
   - When a new message arrives on your phone, it's immediately pushed to the web interface
   - Messages sent from the web are queued and picked up by the Android app

4. **Two-Way Communication**:
   - **Phone → Web**: Messages received on your phone appear in the web interface
   - **Web → Phone**: Messages sent from the web are queued and sent by your phone

### Security Features

- **JWT Authentication**: Secure token-based authentication
- **Encrypted Credentials**: Passwords are hashed using bcrypt
- **HTTPS Support**: Optional SSL/TLS encryption for production
- **Rate Limiting**: Protection against brute force attacks
- **CORS Protection**: Cross-origin request security
- **Input Validation**: All inputs are validated and sanitized

### Components

#### 1. Backend Server (`web-interface/server/`)

The Node.js/Express backend provides:

- **REST API**: Endpoints for authentication, sync, and messaging
- **WebSocket Server**: Real-time updates via Socket.io
- **Database Layer**: TypeORM for PostgreSQL operations
- **Authentication**: JWT token generation and validation
- **File Handling**: MMS attachment upload and serving

**Key Endpoints:**
- `POST /api/auth/register` - Create user account
- `POST /api/auth/login` - Authenticate and get tokens
- `POST /api/sync/initial` - Perform full message sync
- `POST /api/sync/incremental` - Sync new/updated messages
- `GET /api/conversations` - List all conversations
- `POST /api/messages/send` - Send message from web
- `GET /api/sync/queue` - Get messages queued from web

#### 2. Web Client (`web-interface/client/`)

The React/TypeScript frontend provides:

- **Modern UI**: Material Design-inspired interface
- **Real-time Updates**: WebSocket integration for live message updates
- **State Management**: Redux Toolkit for application state
- **Responsive Design**: Works on desktop and mobile browsers

**Features:**
- View all conversations
- Read message history
- Send new messages
- View MMS attachments
- Mark messages as read
- Real-time message updates

#### 3. Android Integration

The Android app includes several key components:

**WebSyncRepository** (`data/src/main/java/com/charles/messenger/repository/WebSyncRepositoryImpl.kt`):
- Interface and implementation for web sync operations
- Handles authentication with JWT tokens
- Batch processing of messages (100 messages per batch)
- Queue polling for web-sent messages
- Message confirmation after sending

**WebSyncService** (`data/src/main/java/com/charles/messenger/service/WebSyncService.kt`):
- Background JobService that runs every 1 minute for periodic sync
- Supports instant sync triggered when messages are sent/received
- Automatically scheduled when web sync is enabled
- Cancelled when web sync is disabled

**SyncToWebServer** (`domain/src/main/java/com/charles/messenger/interactor/SyncToWebServer.kt`):
- Interactor for performing full or incremental syncs
- Returns Flowable with sync progress updates
- Used by both manual sync and background service

**WebSyncSettingsPresenter** (`presentation/src/main/java/com/charles/messenger/feature/settings/websync/WebSyncSettingsPresenter.kt`):
- MVP presenter for web sync settings screen
- Handles connection testing, full sync initiation
- Manages UI state and user interactions

**Key Features:**
- Initial full sync with batch processing (100 messages per batch)
- Incremental sync of new messages (triggered instantly on send/receive)
- Periodic backup sync every 1 minute via background service
- Queue polling for web-sent messages during sync
- Automatic retry on failure
- Progress tracking with detailed status updates
- Thread ID sanitization for system messages
- MMS attachment encoding and upload

### Setup Instructions

#### 1. Server Setup

```bash
# Navigate to web-interface directory
cd web-interface

# Copy environment template
cp .env.example .env

# Generate JWT secrets
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
# Copy the output to JWT_SECRET in .env

node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
# Copy the output to JWT_REFRESH_SECRET in .env

# Start services
docker-compose up -d
```

#### 2. Android App Configuration

1. Open TextPilot app on your phone
2. Go to **Settings** → **Web Sync**
3. Enable **Web Sync**
4. Enter your server URL (e.g., `http://192.168.1.100:8081`)
5. Create a username and password
6. Tap **Test Connection** to verify connectivity
7. Once connected, tap **Perform Initial Sync**

#### 3. Access Web Interface

1. Open your web browser
2. Navigate to your server URL (e.g., `http://192.168.1.100:8081`)
3. Log in with the username and password you created
4. Your messages will appear in the interface

### Technical Details

#### Android Implementation Architecture

The Android app uses a clean architecture pattern with the following components:

**Data Layer** (`data/` module):
- `WebSyncRepositoryImpl`: Implements web sync operations using OkHttp
- `WebSyncService`: Background JobService for periodic and instant sync
- Handles authentication, batch processing, and queue polling

**Domain Layer** (`domain/` module):
- `WebSyncRepository`: Interface defining sync operations
- `SyncToWebServer`: Interactor for full/incremental sync
- `FetchQueuedWebMessages`: Interactor for fetching queued messages
- `TestWebConnection`: Interactor for connection testing

**Presentation Layer** (`presentation/` module):
- `WebSyncSettingsPresenter`: MVP presenter for settings screen
- `WebSyncSettingsView`: View interface
- `WebSyncSettingsController`: Conductor controller for UI

**Sync Triggers**:
- **Instant Sync**: Triggered automatically by:
  - `SmsReceiver` when SMS is received
  - `SmsSentReceiver` when SMS is sent
  - `MmsReceiver` when MMS is received
  - `MmsSentReceiver` when MMS is sent
- **Periodic Sync**: Background JobService runs every 1 minute
- **Boot Sync**: Service automatically scheduled on device boot via `BootReceiver`

**Sync Process**:
1. Authenticate with server using JWT tokens
2. For full sync: Upload all conversations and messages in batches of 100
3. For incremental sync: Upload only messages with timestamp > last sync time
4. Poll queue for web-sent messages during sync
5. Confirm sent messages back to server
6. Update sync token and timestamps

#### Database Schema

The PostgreSQL database stores:

- **Users**: Authentication credentials and settings
- **Conversations**: Thread information and metadata
- **Recipients**: Contact information for conversations
- **Messages**: Message content, timestamps, read status, thread associations
- **Attachments**: MMS media files and metadata
- **SyncState**: Sync tokens, last sync timestamps, progress tracking
- **QueuedMessage**: Messages sent from web waiting to be sent by phone
- **RefreshToken**: JWT refresh tokens for authentication

#### Sync Mechanism

1. **Sync Token**: A UUID that tracks the sync state and ensures consistency
2. **Batch Processing**: Messages are synced in batches of 100 to handle large message histories efficiently
3. **Incremental Updates**: Only new/updated messages are synced after initial sync (based on timestamp)
4. **Queue System**: Web-sent messages are queued on the server and polled by the Android app during sync
5. **Instant Sync**: Automatically triggered when:
   - SMS/MMS messages are received (via SmsReceiver, MmsReceivedReceiver)
   - SMS/MMS messages are sent (via SmsSentReceiver, MmsSentReceiver)
6. **Periodic Sync**: Background JobService runs every 1 minute to ensure all messages are backed up
7. **Boot Scheduling**: Sync service is automatically scheduled on device boot (via BootReceiver)

#### WebSocket Events

The real-time connection uses Socket.io with these events:

- `message:new` - New message received (broadcasted when Android app syncs)
- `message:updated` - Message status updated (read/seen)
- `conversation:updated` - Conversation metadata changed
- `sync:progress` - Sync progress updates (for future use)

#### API Request/Response Formats

**Initial Sync Request** (`POST /api/sync/initial`):
```json
{
  "conversations": [...],  // Only in first batch
  "messages": [...],       // Up to 100 messages per batch
  "batchNumber": 1,
  "totalBatches": 10
}
```

**Incremental Sync Request** (`POST /api/sync/incremental`):
```json
{
  "syncToken": "uuid",
  "newMessages": [...],
  "updatedMessages": [...],
  "deletedMessageIds": []
}
```

**Queue Poll Response** (`GET /api/sync/queue`):
```json
{
  "queuedMessages": [
    {
      "id": "uuid",
      "conversationId": "123",
      "addresses": ["+1234567890"],
      "body": "Message text"
    }
  ]
}
```

### Development Status

#### Completed Features

- ✅ Backend API with full CRUD operations (Node.js/Express/TypeScript)
- ✅ Authentication and authorization (JWT with refresh tokens)
- ✅ Initial and incremental sync with batch processing
- ✅ Message sending from web (queued and sent by Android app)
- ✅ MMS attachment support (upload, download, thumbnail generation)
- ✅ WebSocket real-time updates (Socket.io)
- ✅ Docker deployment (Docker Compose with PostgreSQL, Nginx)
- ✅ Android app integration (background service, instant sync, queue polling)
- ✅ Background periodic sync (every 1 minute)
- ✅ Instant sync on message send/receive
- ✅ Message confirmation system
- ✅ Thread ID sanitization for system messages
- ✅ Error handling and retry logic
- ✅ Progress tracking and status updates

#### In Progress

- 🔄 Web client UI improvements
- 🔄 Message search functionality
- 🔄 Contact photo integration
- 🔄 Enhanced error handling
- 🔄 Performance optimizations

#### Planned Features

- 📋 Message search
- 📋 Contact photos
- 📋 Backup/restore automation
- 📋 Multi-device support
- 📋 End-to-end encryption option
- 📋 Message scheduling from web

### Troubleshooting

#### Connection Issues

- **Check server status**: `docker-compose ps`
- **View server logs**: `docker-compose logs server`
- **Verify firewall**: Ensure port 8081 is accessible
- **Check network**: Use local IP, not localhost (e.g., `http://192.168.1.100:8081`)

#### Sync Issues

- **Check sync status**: View sync status in Android app settings
- **Retry sync**: Tap "Perform Initial Sync" again
- **Check logs**: View server logs for error messages
- **Verify credentials**: Ensure username/password are correct

#### Database Issues

- **Check database**: `docker-compose exec postgres psql -U textpilot -d textpilot_web`
- **View tables**: `\dt` to list all tables
- **Reset database**: `docker-compose down -v` (WARNING: Deletes all data)

### Security Considerations

1. **Use HTTPS in Production**: Set up SSL certificates for production use
2. **Strong Passwords**: Use complex passwords for user accounts
3. **Network Security**: Limit access to your server using firewall rules
4. **Regular Updates**: Keep Docker images and dependencies updated
5. **Backup Data**: Regularly backup your PostgreSQL database

### Future Enhancements

- End-to-end encryption for messages
- Multi-user support with role-based access
- Message search and filtering
- Contact management
- Scheduled messages from web
- Mobile app for iOS/Android web interface
- Push notifications for new messages

## Contributing

The Web SMS feature is part of the TextPilot project. Contributions are welcome! See the main [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## Support

For issues or questions about the Web SMS feature:
- File an issue on [GitHub](https://github.com/chartmann1590/textpilot/issues)
- Check the [main README](../README.md) for general information
- Review the [web-interface README](../web-interface/README.md) for detailed setup instructions

---

**Note**: This feature is in active development. Some features may not be fully implemented or may change in future releases.

