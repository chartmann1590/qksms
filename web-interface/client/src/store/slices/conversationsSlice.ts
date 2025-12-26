import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '../../services/api';
import type { Conversation } from '../../types';

interface ConversationsState {
  conversations: Conversation[];
  selectedConversationId: string | null;
  loading: boolean;
  error: string | null;
  page: number;
  hasMore: boolean;
}

const initialState: ConversationsState = {
  conversations: [],
  selectedConversationId: null,
  loading: false,
  error: null,
  page: 1,
  hasMore: true,
};

// Async thunks
export const fetchConversations = createAsyncThunk(
  'conversations/fetchConversations',
  async (params: { page?: number; search?: string } = {}, { rejectWithValue }) => {
    try {
      const { page = 1, search = '' } = params;
      const response = await apiClient.getConversations(page, 1000, search); // Increased to 1000
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.error || 'Failed to fetch conversations');
    }
  }
);

// Slice
const conversationsSlice = createSlice({
  name: 'conversations',
  initialState,
  reducers: {
    selectConversation: (state, action: PayloadAction<string>) => {
      state.selectedConversationId = action.payload;
    },
    updateConversation: (state, action: PayloadAction<Conversation>) => {
      const index = state.conversations.findIndex((c) => c.id === action.payload.id);
      if (index !== -1) {
        state.conversations[index] = action.payload;
      } else {
        state.conversations.unshift(action.payload);
      }
    },
    incrementUnreadCount: (state, action: PayloadAction<string>) => {
      const conversation = state.conversations.find((c) => c.id === action.payload);
      if (conversation) {
        conversation.unreadCount += 1;
      }
    },
    clearUnreadCount: (state, action: PayloadAction<string>) => {
      const conversation = state.conversations.find((c) => c.id === action.payload);
      if (conversation) {
        conversation.unreadCount = 0;
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchConversations.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchConversations.fulfilled, (state, action) => {
        state.loading = false;
        const isFirstPage = action.meta.arg.page === 1 || !action.meta.arg.page;
        if (isFirstPage) {
          // First page or search, replace all
          state.conversations = action.payload.conversations;
        } else {
          // Append to existing
          state.conversations.push(...action.payload.conversations);
        }
        state.page = action.payload.page;
        state.hasMore = action.payload.conversations.length === 1000;
      })
      .addCase(fetchConversations.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { selectConversation, updateConversation, incrementUnreadCount, clearUnreadCount } =
  conversationsSlice.actions;
export default conversationsSlice.reducer;
