# TextPilot Web SMS Feature

## Overview

The TextPilot Web SMS feature allows you to access and manage your SMS/MMS messages from any web browser. This self-hosted solution provides a secure, real-time interface that syncs with your Android device, enabling you to send and receive messages from your computer or any device with a web browser.

## Status: In Development

The Web SMS feature is currently in active development. The backend infrastructure is complete and functional, with the web client interface being continuously improved.

### Current Implementation Status

- ✅ **Backend Server** - Fully functional REST API with authentication
- ✅ **Database** - PostgreSQL database for message storage
- ✅ **Real-time Sync** - WebSocket support for instant updates
- ✅ **Android Integration** - Full sync support from TextPilot Android app
- ✅ **Web Client** - React-based interface (in development)
- ✅ **Docker Deployment** - One-command setup with Docker Compose
- ✅ **Security** - JWT authentication, encrypted credentials, rate limiting
- ✅ **MMS Support** - Attachment handling and media support

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
   - New messages received on your phone are automatically synced to the server
   - The app periodically checks for messages sent from the web interface
   - Only changes are transmitted, making syncs fast and efficient

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

#### 3. Android Integration (`data/src/main/java/com/charles/messenger/repository/WebSyncRepositoryImpl.kt`)

The Android app includes:

- **WebSyncRepository**: Interface for web sync operations
- **Sync Service**: Background service for periodic syncing
- **Settings UI**: Configuration screen for web sync settings
- **Batch Processing**: Efficient message upload in batches

**Key Features:**
- Initial full sync
- Incremental sync of new messages
- Queue polling for web-sent messages
- Automatic retry on failure
- Progress tracking

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

#### Database Schema

The PostgreSQL database stores:

- **Users**: Authentication credentials and settings
- **Conversations**: Thread information and metadata
- **Messages**: Message content, timestamps, read status
- **Attachments**: MMS media files and metadata
- **Sync State**: Sync tokens and last sync timestamps
- **Message Queue**: Messages sent from web waiting to be sent by phone

#### Sync Mechanism

1. **Sync Token**: A UUID that tracks the sync state
2. **Batch Processing**: Messages are synced in batches to handle large message histories
3. **Incremental Updates**: Only new/updated messages are synced after initial sync
4. **Queue System**: Web-sent messages are queued and polled by the Android app

#### WebSocket Events

The real-time connection uses Socket.io with these events:

- `message:new` - New message received
- `message:updated` - Message status updated (read/seen)
- `conversation:updated` - Conversation metadata changed
- `sync:progress` - Sync progress updates

### Development Status

#### Completed Features

- ✅ Backend API with full CRUD operations
- ✅ Authentication and authorization
- ✅ Initial and incremental sync
- ✅ Message sending from web
- ✅ MMS attachment support
- ✅ WebSocket real-time updates
- ✅ Docker deployment
- ✅ Android app integration

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
- File an issue on [GitHub](https://github.com/chartmann1590/qksms/issues)
- Check the [main README](../README.md) for general information
- Review the [web-interface README](../web-interface/README.md) for detailed setup instructions

---

**Note**: This feature is in active development. Some features may not be fully implemented or may change in future releases.

