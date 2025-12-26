# TextPilot Web Interface

Self-hosted web interface for TextPilot Android SMS app with real-time message syncing.

## Features

- 🔒 **Self-hosted**: Complete control over your messaging data
- 🔐 **Secure**: End-to-end authentication with JWT tokens and encrypted credentials
- ⚡ **Real-time**: WebSocket-based instant message updates
- 📱 **Two-way sync**: Send and receive messages from both phone and web
- 🖼️ **MMS Support**: View and send multimedia messages with attachments
- 🐳 **Easy deployment**: One-command Docker setup

## Prerequisites

- Docker and Docker Compose
- TextPilot Android app installed on your phone
- Node.js 18+ (for development only)

## Quick Start

### 1. Initial Setup

```bash
# Navigate to web-interface directory
cd web-interface

# Copy environment template
cp .env.example .env

# Edit .env and set your secrets
nano .env  # or use any text editor
```

**Important**: Generate strong random secrets for JWT tokens:

```bash
# Generate JWT_SECRET
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Generate JWT_REFRESH_SECRET
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

Add these to your `.env` file.

### 2. Start the Services

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

The services will be available at:
- **Web Interface**: http://localhost:8081
- **API**: http://localhost:8081/api
- **Health Check**: http://localhost:8081/health

### 3. Configure Android App

1. Open TextPilot app on your phone
2. Go to **Settings** → **Web Sync**
3. Enable **Web Sync**
4. Enter your server URL (e.g., `http://your-server-ip:8080`)
5. Create a username and password
6. Tap **Test Connection**
7. Once connected, tap **Perform Initial Sync**

## Architecture

### Components

- **PostgreSQL**: Message and user data storage
- **Node.js/Express**: RESTful API backend
- **React + TypeScript**: Web client interface
- **Socket.io**: Real-time WebSocket communication
- **Nginx**: Reverse proxy and static file serving

### Data Flow

```
Android App ←→ Server API ←→ PostgreSQL
                ↓
         WebSocket Server
                ↓
           Web Client
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Create user account
- `POST /api/auth/login` - Login and receive tokens
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout and invalidate token

### Sync

- `POST /api/sync/initial` - Initial full sync from phone
- `POST /api/sync/incremental` - Incremental updates
- `GET /api/sync/status` - Get sync state
- `GET /api/sync/queue` - Poll for web-sent messages
- `POST /api/sync/confirm` - Confirm message sent

### Messages

- `GET /api/conversations` - List all conversations
- `GET /api/conversations/:id` - Get conversation details
- `GET /api/conversations/:id/messages` - Get conversation messages (via messages controller)
- `POST /api/messages/send` - Send message from web
- `PATCH /api/messages/status` - Mark messages as read

### Attachments

- `POST /api/attachments/upload` - Upload attachment file
- `GET /api/attachments/:id` - Download attachment
- `GET /api/attachments/:id/thumbnail` - Get image thumbnail
- `GET /api/attachments/by-upload/:uploadId` - Get attachment by upload ID

## Development

### Running Locally

```bash
# Server development
cd server
npm install
cp .env.example .env  # Configure environment
npm run dev

# Client development (coming soon)
cd client
npm install
npm run dev
```

### Database Management

```bash
# Access PostgreSQL console
docker-compose exec postgres psql -U qksms -d qksms_web

# Backup database
docker-compose exec postgres pg_dump -U qksms qksms_web > backup.sql

# Restore database
docker-compose exec -T postgres psql -U qksms qksms_web < backup.sql

# View logs
docker-compose logs postgres
```

### Rebuilding Services

```bash
# Rebuild specific service
docker-compose build server
docker-compose up -d server

# Rebuild all services
docker-compose build
docker-compose up -d
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_USER` | Database username | `qksms` |
| `POSTGRES_PASSWORD` | Database password | `changeme` |
| `POSTGRES_DB` | Database name | `qksms_web` |
| `JWT_SECRET` | JWT access token secret | *Required* |
| `JWT_REFRESH_SECRET` | JWT refresh token secret | *Required* |
| `HTTP_PORT` | HTTP port for web interface | `8080` |
| `NODE_ENV` | Environment mode | `production` |
| `MAX_FILE_SIZE` | Max upload size in bytes | `10485760` (10MB) |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per minute | `100` |

### HTTPS Setup (Optional but Recommended)

1. Generate or obtain SSL certificate:

```bash
# Self-signed certificate (for testing)
mkdir ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/key.pem -out ssl/cert.pem
```

2. Uncomment HTTPS sections in `docker-compose.yml` and `docker/nginx.conf`

3. Restart services:

```bash
docker-compose down
docker-compose up -d
```

Access at: https://localhost:8443

## Security

### Best Practices

1. **Use strong passwords**: Minimum 12 characters
2. **Generate random JWT secrets**: Use crypto.randomBytes(32)
3. **Enable HTTPS in production**: Use Let's Encrypt or self-signed certs
4. **Change default database password**
5. **Keep Docker images updated**: `docker-compose pull`
6. **Limit network exposure**: Use firewall rules
7. **Regular backups**: Backup PostgreSQL data

### Network Security

If hosting on a server:

```bash
# Allow only your phone's IP
sudo ufw allow from YOUR_PHONE_IP to any port 8080

# Or use VPN/Tailscale for secure remote access
```

## Troubleshooting

### Connection Issues

```bash
# Check service status
docker-compose ps

# View server logs
docker-compose logs server

# Test database connection
docker-compose exec server npm run typeorm migration:show

# Restart all services
docker-compose restart
```

### Android App Can't Connect

1. Ensure server is running: `docker-compose ps`
2. Check firewall allows port 8080
3. Use local IP, not localhost (e.g., `http://192.168.1.100:8080`)
4. Check server logs: `docker-compose logs server`
5. Verify .env has valid JWT secrets

### Database Issues

```bash
# Reset database (WARNING: Deletes all data)
docker-compose down -v
docker-compose up -d

# Check database tables
docker-compose exec postgres psql -U qksms -d qksms_web -c "\dt"
```

## Implementation Status

- [x] Backend server with authentication
- [x] PostgreSQL database schema
- [x] Docker Compose setup
- [x] Android app settings screen
- [x] Sync endpoints (initial + incremental)
- [x] React web client
- [x] WebSocket real-time updates
- [x] MMS attachment handling
- [x] Input validation and error handling
- [x] Security hardening (CORS, rate limiting, XSS prevention)
- [ ] Message search
- [ ] Contact photos
- [ ] Backup/restore automation

## Contributing

This is part of the TextPilot project. Contributions welcome!

## License

GNU General Public License v3.0 - Same as TextPilot

## Support

For issues, please file a bug report on the [TextPilot GitHub repository](https://github.com/chartmann1590/qksms).
