import { io, Socket } from 'socket.io-client';

export interface WebSocketEvent {
  type: 'NEW_MESSAGE' | 'MESSAGE_SENT' | 'MESSAGE_STATUS_CHANGED' | 'CONVERSATION_UPDATED';
  payload: any;
}

type EventCallback = (event: WebSocketEvent) => void;

class WebSocketClient {
  private socket: Socket | null = null;
  private callbacks: Set<EventCallback> = new Set();
  private isConnecting = false;

  connect(accessToken: string) {
    if (this.socket?.connected || this.isConnecting) {
      return;
    }

    this.isConnecting = true;

    const serverUrl = import.meta.env.VITE_SERVER_URL || 'http://localhost:3000';

    this.socket = io(serverUrl, {
      auth: {
        token: accessToken,
      },
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionAttempts: 10,
    });

    this.socket.on('connect', () => {
      console.log('✓ WebSocket connected');
      this.isConnecting = false;
    });

    this.socket.on('disconnect', () => {
      console.log('✗ WebSocket disconnected');
      this.isConnecting = false;
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error.message);
      this.isConnecting = false;
    });

    // Listen for all event types
    this.socket.on('new_message', (event: WebSocketEvent) => {
      console.log('New message received:', event);
      this.notifyCallbacks(event);
    });

    this.socket.on('message_sent', (event: WebSocketEvent) => {
      console.log('Message sent confirmation:', event);
      this.notifyCallbacks(event);
    });

    this.socket.on('message_status_changed', (event: WebSocketEvent) => {
      console.log('Message status changed:', event);
      this.notifyCallbacks(event);
    });

    this.socket.on('conversation_updated', (event: WebSocketEvent) => {
      console.log('Conversation updated:', event);
      this.notifyCallbacks(event);
    });
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
    this.callbacks.clear();
    this.isConnecting = false;
  }

  subscribe(callback: EventCallback) {
    this.callbacks.add(callback);

    // Return unsubscribe function
    return () => {
      this.callbacks.delete(callback);
    };
  }

  private notifyCallbacks(event: WebSocketEvent) {
    this.callbacks.forEach((callback) => {
      try {
        callback(event);
      } catch (error) {
        console.error('Error in WebSocket callback:', error);
      }
    });
  }

  // Emit events to server (future use)
  markAsRead(messageIds: string[]) {
    if (this.socket?.connected) {
      this.socket.emit('mark_read', { messageIds });
    }
  }

  typing(conversationId: string, isTyping: boolean) {
    if (this.socket?.connected) {
      this.socket.emit('typing', { conversationId, isTyping });
    }
  }

  isConnected(): boolean {
    return this.socket?.connected || false;
  }
}

export const websocketClient = new WebSocketClient();
