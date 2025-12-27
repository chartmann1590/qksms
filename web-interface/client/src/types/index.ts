// User and Authentication types
export interface User {
  id: string;
  username: string;
  deviceId: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  deviceId: string;
}

export interface LoginResponse {
  user: User;
  tokens: AuthTokens;
}

// Conversation types
export interface Conversation {
  id: string;
  name?: string;
  recipients: Recipient[];
  lastMessage?: Message;
  lastMessageDate?: string;
  archived: boolean;
  blocked: boolean;
  pinned: boolean;
  unreadCount: number;
}

export interface Recipient {
  id: string;
  address: string;
  name?: string;
}

// Message types
export interface Message {
  id: string;
  conversationId: string;
  address: string;
  body?: string;
  type: 'sms' | 'mms';
  date: string;
  dateSent?: string;
  read: boolean;
  seen: boolean;
  isMe: boolean;
  attachments?: Attachment[];
}

export interface Attachment {
  id: number;
  mimeType: string;
  fileSize?: number;
  hasThumbnail: boolean;
}

// API response types
export interface ConversationListResponse {
  conversations: Conversation[];
  total: number;
  page: number;
  limit: number;
}

export interface MessagesResponse {
  messages: Message[];
  total: number;
  page: number;
  limit: number;
}

export interface SendMessageRequest {
  conversationId?: string;
  addresses: string[];
  body: string;
}

export interface SendMessageResponse {
  queueId: string;
  message: string;
}

// Sync types
export interface SyncStatus {
  lastSyncTime: string | null;
  lastFullSync: string | null;
  lastIncrementalSync: string | null;
  messageCount: number;
  conversationCount: number;
  syncInProgress: boolean;
}

// WebSocket event types
export interface WebSocketMessage {
  type: 'NEW_MESSAGE' | 'MESSAGE_SENT' | 'MESSAGE_STATUS_CHANGED' | 'CONVERSATION_UPDATED';
  payload: any;
}
