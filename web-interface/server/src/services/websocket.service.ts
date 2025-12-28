import { Server as HttpServer } from 'http';
import { Server as SocketIOServer, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { Message } from '../models/Message';
import { Conversation } from '../models/Conversation';

interface AuthenticatedSocket extends Socket {
  userId?: string;
}

export class WebSocketService {
  private io: SocketIOServer;
  private userSockets: Map<string, Set<string>> = new Map(); // userId -> Set of socketIds

  constructor(httpServer: HttpServer) {
    this.io = new SocketIOServer(httpServer, {
      cors: {
        origin: process.env.CLIENT_URL || 'http://localhost:3001',
        credentials: true,
      },
    });

    this.setupMiddleware();
    this.setupEventHandlers();
  }

  private setupMiddleware() {
    // Authentication middleware for WebSocket connections
    this.io.use((socket: AuthenticatedSocket, next) => {
      const token = socket.handshake.auth.token;

      if (!token) {
        return next(new Error('Authentication required'));
      }

      try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET!) as { userId: string };
        socket.userId = decoded.userId;
        next();
      } catch (error) {
        next(new Error('Invalid token'));
      }
    });
  }

  private setupEventHandlers() {
    this.io.on('connection', (socket: AuthenticatedSocket) => {
      const userId = socket.userId!;
      console.log(`WebSocket connected: userId=${userId}, socketId=${socket.id}`);

      // Track user's socket
      if (!this.userSockets.has(userId)) {
        this.userSockets.set(userId, new Set());
      }
      this.userSockets.get(userId)!.add(socket.id);

      // Join user-specific room
      socket.join(`user:${userId}`);

      // Handle disconnection
      socket.on('disconnect', () => {
        console.log(`WebSocket disconnected: userId=${userId}, socketId=${socket.id}`);
        const userSocketSet = this.userSockets.get(userId);
        if (userSocketSet) {
          userSocketSet.delete(socket.id);
          if (userSocketSet.size === 0) {
            this.userSockets.delete(userId);
          }
        }
      });

      // Handle mark messages as read (from client)
      socket.on('mark_read', (data: { messageIds: string[] }) => {
        console.log(`Mark read from client: ${data.messageIds.length} messages`);
        // This is handled by the API endpoint, but we can broadcast to other clients
        this.broadcastToUser(userId, 'messages_read', {
          messageIds: data.messageIds,
        });
      });

      // Handle typing indicator (optional future feature)
      socket.on('typing', (data: { conversationId: string; isTyping: boolean }) => {
        this.broadcastToUser(userId, 'typing', data);
      });
    });
  }

  /**
   * Broadcast new message to user's connected clients
   */
  public broadcastNewMessage(userId: string, message: Message) {
    const roomName = `user:${userId}`;
    const socketsInRoom = this.io.sockets.adapter.rooms.get(roomName);
    const socketCount = socketsInRoom ? socketsInRoom.size : 0;
    console.log(`Broadcasting new message to user ${userId}: messageId=${message.id}, sockets in room: ${socketCount}`);

    this.io.to(roomName).emit('new_message', {
      type: 'NEW_MESSAGE',
      payload: {
        id: message.id,
        conversationId: message.conversationId,
        address: message.address,
        body: message.body,
        type: message.type,
        date: message.date,
        dateSent: message.dateSent,
        read: message.read,
        seen: message.seen,
        isMe: message.isMe,
      },
    });
  }

  /**
   * Broadcast message sent confirmation (when Android confirms queue message was sent)
   */
  public broadcastMessageSent(
    userId: string,
    queueId: string,
    androidMessageId: string,
    message: Message
  ) {
    console.log(
      `Broadcasting message sent confirmation to user ${userId}: queueId=${queueId}, androidMessageId=${androidMessageId}`
    );
    this.io.to(`user:${userId}`).emit('message_sent', {
      type: 'MESSAGE_SENT',
      payload: {
        queueId,
        androidMessageId,
        message: {
          id: message.id,
          conversationId: message.conversationId,
          address: message.address,
          body: message.body,
          type: message.type,
          date: message.date,
          dateSent: message.dateSent,
          read: message.read,
          seen: message.seen,
          isMe: message.isMe,
        },
      },
    });
  }

  /**
   * Broadcast message status change (read/seen)
   */
  public broadcastMessageStatusChanged(
    userId: string,
    messageIds: string[],
    updates: { read?: boolean; seen?: boolean }
  ) {
    console.log(`Broadcasting message status change to user ${userId}: ${messageIds.length} messages`);
    this.io.to(`user:${userId}`).emit('message_status_changed', {
      type: 'MESSAGE_STATUS_CHANGED',
      payload: {
        messageIds,
        updates,
      },
    });
  }

  /**
   * Broadcast conversation update
   */
  public broadcastConversationUpdated(userId: string, conversation: Conversation) {
    console.log(`Broadcasting conversation update to user ${userId}: conversationId=${conversation.id}`);
    this.io.to(`user:${userId}`).emit('conversation_updated', {
      type: 'CONVERSATION_UPDATED',
      payload: {
        id: conversation.id,
        name: conversation.name,
        archived: conversation.archived,
        blocked: conversation.blocked,
        pinned: conversation.pinned,
        lastMessageDate: conversation.lastMessageDate,
      },
    });
  }

  /**
   * Broadcast to all of a user's connected clients except one
   */
  private broadcastToUser(userId: string, event: string, data: any, exceptSocketId?: string) {
    const userSocketSet = this.userSockets.get(userId);
    if (!userSocketSet) return;

    userSocketSet.forEach((socketId) => {
      if (socketId !== exceptSocketId) {
        this.io.to(socketId).emit(event, data);
      }
    });
  }

  /**
   * Get connected socket count for a user
   */
  public getUserSocketCount(userId: string): number {
    return this.userSockets.get(userId)?.size || 0;
  }

  /**
   * Get total connected clients
   */
  public getTotalConnections(): number {
    return this.io.sockets.sockets.size;
  }
}

let websocketService: WebSocketService;

export function initializeWebSocket(httpServer: HttpServer): WebSocketService {
  websocketService = new WebSocketService(httpServer);
  console.log('✓ WebSocket service initialized');
  return websocketService;
}

export function getWebSocketService(): WebSocketService {
  if (!websocketService) {
    throw new Error('WebSocket service not initialized');
  }
  return websocketService;
}
