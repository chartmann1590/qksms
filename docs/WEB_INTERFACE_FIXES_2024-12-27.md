# Web Interface Fixes - December 27, 2024

## Summary
Fixed critical issues with the web interface search functionality and WebSocket connection.

## Fixed Issues

### 1. WebSocket Connection Error
**Problem:** WebSocket client was attempting to connect directly to `http://localhost:3000` instead of using the same origin through nginx proxy.

**Solution:** 
- Updated `web-interface/client/src/services/websocket.ts` to use `window.location.origin` when available, allowing the WebSocket to connect through nginx at `/socket.io/` endpoint
- WebSocket now correctly connects to the same origin in production/Docker environments

**Files Changed:**
- `web-interface/client/src/services/websocket.ts`

### 2. Search Functionality Not Working
**Problem:** Search parameter was not being sent to the server, and when it was sent, the server returned a 500 error due to incorrect SQL column names.

**Solution:**
- **Frontend:** Fixed search parameter flow from component → Redux thunk → API client
  - Consolidated duplicate `useEffect` hooks in `ConversationList` component
  - Ensured search query is properly passed through the entire chain
  - Fixed debounce logic to trigger correctly when search query changes
  
- **Backend:** Fixed SQL query in conversations controller
  - Changed column references from camelCase to snake_case to match database schema
  - Updated `conversationId` → `conversation_id`
  - Updated `userId` → `user_id`
  - Search now correctly filters conversations by name, recipient address/name, and message body

**Files Changed:**
- `web-interface/client/src/components/ConversationList/ConversationList.tsx`
- `web-interface/client/src/store/slices/conversationsSlice.ts`
- `web-interface/client/src/services/api.ts`
- `web-interface/server/src/controllers/conversations.controller.ts`

## Testing
- Verified WebSocket connects successfully through nginx proxy
- Verified search functionality works with various search terms
- Verified search filters conversations by name, phone number, and message content
- Tested in Docker container environment

## Deployment Notes
- Rebuild Docker containers after these changes:
  ```bash
  cd web-interface
  docker-compose build
  docker-compose up -d
  ```

