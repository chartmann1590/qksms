import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '../../services/api';
import type { Message, SendMessageRequest } from '../../types';

interface MessagesState {
  messagesByConversation: Record<string, Message[]>;
  loading: boolean;
  sending: boolean;
  error: string | null;
}

const initialState: MessagesState = {
  messagesByConversation: {},
  loading: false,
  sending: false,
  error: null,
};

// Async thunks
export const fetchMessages = createAsyncThunk(
  'messages/fetchMessages',
  async ({ conversationId, page = 1 }: { conversationId: string; page?: number }, { rejectWithValue }) => {
    try {
      const response = await apiClient.getMessages(conversationId, page, 100);
      return { conversationId, messages: response.messages, page };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.error || 'Failed to fetch messages');
    }
  }
);

export const sendMessage = createAsyncThunk(
  'messages/sendMessage',
  async (request: SendMessageRequest, { rejectWithValue }) => {
    try {
      const response = await apiClient.sendMessage(request);
      return { request, response };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.error || 'Failed to send message');
    }
  }
);

export const markMessagesAsRead = createAsyncThunk(
  'messages/markAsRead',
  async ({ conversationId, messageIds }: { conversationId: string; messageIds: string[] }, { rejectWithValue }) => {
    try {
      await apiClient.markAsRead(messageIds);
      return { conversationId, messageIds };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.error || 'Failed to mark as read');
    }
  }
);

// Slice
const messagesSlice = createSlice({
  name: 'messages',
  initialState,
  reducers: {
    addMessage: (state, action: PayloadAction<Message>) => {
      const { conversationId } = action.payload;
      if (!state.messagesByConversation[conversationId]) {
        state.messagesByConversation[conversationId] = [];
      }

      // Check if message already exists (avoid duplicates)
      const exists = state.messagesByConversation[conversationId].some(
        (m) => m.id === action.payload.id
      );

      if (!exists) {
        state.messagesByConversation[conversationId].push(action.payload);
        // Sort by date (newest last)
        state.messagesByConversation[conversationId].sort(
          (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
        );
      }
    },
    updateMessage: (state, action: PayloadAction<Message>) => {
      const { conversationId, id } = action.payload;
      if (state.messagesByConversation[conversationId]) {
        const index = state.messagesByConversation[conversationId].findIndex((m) => m.id === id);
        if (index !== -1) {
          state.messagesByConversation[conversationId][index] = action.payload;
        }
      }
    },
    clearMessages: (state, action: PayloadAction<string>) => {
      delete state.messagesByConversation[action.payload];
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch messages
      .addCase(fetchMessages.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchMessages.fulfilled, (state, action) => {
        state.loading = false;
        const { conversationId, messages, page } = action.payload;

        if (page === 1) {
          // First page, replace all
          state.messagesByConversation[conversationId] = messages;
        } else {
          // Prepend older messages
          state.messagesByConversation[conversationId] = [
            ...messages,
            ...(state.messagesByConversation[conversationId] || []),
          ];
        }

        // Sort by date (newest last)
        state.messagesByConversation[conversationId].sort(
          (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
        );
      })
      .addCase(fetchMessages.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Send message
      .addCase(sendMessage.pending, (state) => {
        state.sending = true;
        state.error = null;
      })
      .addCase(sendMessage.fulfilled, (state) => {
        state.sending = false;
      })
      .addCase(sendMessage.rejected, (state, action) => {
        state.sending = false;
        state.error = action.payload as string;
      })
      // Mark as read
      .addCase(markMessagesAsRead.fulfilled, (state, action) => {
        const { conversationId, messageIds } = action.payload;
        if (state.messagesByConversation[conversationId]) {
          state.messagesByConversation[conversationId].forEach((msg) => {
            if (messageIds.includes(msg.id)) {
              msg.read = true;
            }
          });
        }
      });
  },
});

export const { addMessage, updateMessage, clearMessages } = messagesSlice.actions;
export default messagesSlice.reducer;
