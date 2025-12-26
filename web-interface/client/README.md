# TextPilot Web Client

React-based web interface for viewing and sending SMS/MMS messages synced from your TextPilot Android app.

## Features

- 📱 View all conversations and messages
- 💬 Send messages from your browser
- 🔄 Real-time sync with Android app
- 🔐 Secure authentication with JWT tokens
- 📊 Clean, modern Material-inspired UI
- 📱 Responsive design (desktop & mobile)

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Redux Toolkit** - State management
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **date-fns** - Date formatting

## Development Setup

### Prerequisites

- Node.js 18+ and npm
- TextPilot server running (see `../server/README.md`)

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:3001`

### Build for Production

```bash
npm run build
```

Production files will be in the `dist/` directory.

## Usage

1. **Login**: Enter your TextPilot server credentials
2. **View Conversations**: Browse your synced conversations in the left sidebar
3. **Read Messages**: Click a conversation to view message history
4. **Send Messages**: Type in the compose box at the bottom and click Send
5. **Auto-sync**: Messages are automatically marked as read when viewed

## Configuration

The API endpoint is configured in `vite.config.ts`:

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:3000',
    changeOrigin: true,
  },
}
```

For production, update this to point to your deployed server.

## Project Structure

```
src/
├── components/          # React components
│   ├── Login/          # Login page
│   ├── ConversationList/ # Sidebar conversation list
│   └── MessageThread/  # Message view and compose
├── store/              # Redux store
│   └── slices/         # Redux slices (auth, conversations, messages)
├── services/           # API client
├── types/              # TypeScript interfaces
├── App.tsx             # Main app component with routing
└── main.tsx            # Entry point
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Authentication

The app uses JWT-based authentication:

- Access tokens expire after 30 days
- Refresh tokens expire after 90 days
- Tokens are stored in localStorage
- Automatic token refresh on 401 responses

## Future Enhancements

- [ ] WebSocket integration for real-time updates
- [ ] MMS attachment viewing
- [ ] Search functionality
- [ ] Dark mode
- [ ] Push notifications
- [ ] Message drafts
- [ ] Emoji picker
