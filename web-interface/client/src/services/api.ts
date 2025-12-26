import axios, { AxiosInstance, AxiosError } from 'axios';
import type {
  LoginRequest,
  LoginResponse,
  ConversationListResponse,
  MessagesResponse,
  SendMessageRequest,
  SendMessageResponse,
  SyncStatus,
} from '../types';

class ApiClient {
  private client: AxiosInstance;
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: '/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Load tokens from localStorage
    this.accessToken = localStorage.getItem('accessToken');
    this.refreshToken = localStorage.getItem('refreshToken');

    // Request interceptor to add auth token
    this.client.interceptors.request.use(
      (config) => {
        if (this.accessToken) {
          config.headers.Authorization = `Bearer ${this.accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor to handle token refresh
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config;

        // If 401 and we have a refresh token, try to refresh
        if (error.response?.status === 401 && this.refreshToken && originalRequest) {
          try {
            const { data } = await axios.post('/api/auth/refresh', {
              refreshToken: this.refreshToken,
            });

            this.setTokens(data.accessToken, data.refreshToken);

            // Retry original request with new token
            originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
            return axios(originalRequest);
          } catch (refreshError) {
            // Refresh failed, logout user
            this.logout();
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  setTokens(accessToken: string, refreshToken: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  clearTokens() {
    this.accessToken = null;
    this.refreshToken = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  /**
   * Get or generate a device ID for this browser
   * The device ID is stored in localStorage and persists across sessions
   */
  private getOrCreateDeviceId(): string {
    const STORAGE_KEY = 'textpilot_device_id';
    let deviceId = localStorage.getItem(STORAGE_KEY);
    
    if (!deviceId) {
      // Generate a simple device ID (16 hex characters, similar to Android format)
      deviceId = Array.from(crypto.getRandomValues(new Uint8Array(8)))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');
      localStorage.setItem(STORAGE_KEY, deviceId);
    }
    
    return deviceId;
  }

  // Auth endpoints
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    // Ensure deviceId is included in the request
    const loginData = {
      ...credentials,
      deviceId: credentials.deviceId || this.getOrCreateDeviceId(),
    };
    const { data } = await this.client.post<LoginResponse>('/auth/login', loginData);
    this.setTokens(data.tokens.accessToken, data.tokens.refreshToken);
    return data;
  }

  async logout(): Promise<void> {
    try {
      if (this.refreshToken) {
        await this.client.post('/auth/logout', { refreshToken: this.refreshToken });
      }
    } finally {
      this.clearTokens();
    }
  }

  // Conversation endpoints
  async getConversations(page = 1, limit = 1000, search = ''): Promise<ConversationListResponse> {
    const params: any = { page, limit };
    if (search && search.trim()) {
      params.search = search.trim();
    }
    const { data } = await this.client.get<ConversationListResponse>('/conversations', {
      params,
    });
    return data;
  }

  async getConversation(id: string): Promise<any> {
    const { data } = await this.client.get(`/conversations/${id}`);
    return data;
  }

  // Message endpoints
  async getMessages(conversationId: string, page = 1, limit = 100): Promise<MessagesResponse> {
    const { data } = await this.client.get<MessagesResponse>(
      `/conversations/${conversationId}/messages`,
      {
        params: { page, limit },
      }
    );
    return data;
  }

  async sendMessage(request: SendMessageRequest): Promise<SendMessageResponse> {
    const { data } = await this.client.post<SendMessageResponse>('/messages/send', request);
    return data;
  }

  async markAsRead(messageIds: string[]): Promise<void> {
    await this.client.patch('/messages/status', {
      messageIds,
      read: true,
    });
  }

  async markAsSeen(messageIds: string[]): Promise<void> {
    await this.client.patch('/messages/status', {
      messageIds,
      seen: true,
    });
  }

  // Sync endpoints
  async getSyncStatus(): Promise<SyncStatus> {
    const { data } = await this.client.get<SyncStatus>('/sync/status');
    return data;
  }

  // Attachment endpoints
  async uploadAttachment(file: File): Promise<{ uploadId: string }> {
    const formData = new FormData();
    formData.append('file', file);

    const { data } = await this.client.post('/attachments/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return data;
  }

  getAttachmentUrl(id: string): string {
    return `/api/attachments/${id}`;
  }

  getThumbnailUrl(id: string): string {
    return `/api/attachments/${id}/thumbnail`;
  }

  isAuthenticated(): boolean {
    return !!this.accessToken;
  }
}

export const apiClient = new ApiClient();
